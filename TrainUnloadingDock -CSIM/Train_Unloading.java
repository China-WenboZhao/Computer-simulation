import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.Scanner;

import com.mesquite.csim.Event;
import com.mesquite.csim.FCFSFacility;
import com.mesquite.csim.Model;
import com.mesquite.csim.Table;

public class Train_Unloading extends Model {

	public static void main(String[] args) {
		Train_Unloading tu = new Train_Unloading();
		if (args.length == 2) {
			inputcase = 1;
			ave_arr_interval = Double.parseDouble(args[0]);
			max_arr_time = Double.parseDouble(args[1]);
			Scanner sc = new Scanner(System.in);
			System.out.println(
					"input the kind of running: 1.run simulation for one time 2.run simulation for 100 times 3. run simulations until accuracy of 1%, with 99% confidence");
			type = sc.nextInt();
		} else if (args.length == 3) {
			inputcase = 2;
			fname1 = args[1];
			fname2 = args[2];
			type = 1;
		} else {
			return;
		}

		tu.run();
		tu.report();
	}

	public void run() {

		start(new Sim());
		if (inputcase == 2) {
			try {
				r1.close();
				r2.close();
				buf1.close();
				buf2.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private FCFSFacility f;
	LinkedList<Train> l;
	LinkedList<Event> e;
	Event depart;
	static int Trainindex = 0;
	static double depart_time = 0;

	/* variables used as input */
	static double ave_arr_interval = 0;
	static double max_arr_time = 0;
	static String fname1 = "";
	static String fname2 = "";

	/* variables used in statistic at the end */
	static double total_in_sys_time = 0;
	static double max_in_sys_time = 0;
	static double total_busy_time = 0;
	static double total_idle_time = 0;
	static double total_hogged_out_time = 0;
	static double latest_arr_or_depart_time = 0; // to compute the time average trains in dock, we need to calculate the
													// area when train arrive or depart,this variable is used to record
													// latest time of arrive or depart
	static double area = 0;
	static int max_trains_in_queue = 0;

	/* related to file input */
	static int inputcase;
	static int type = 0;
	File file1 = null;
	Reader r1 = null;
	BufferedReader buf1 = null;
	File file2 = null;
	Reader r2 = null;
	BufferedReader buf2 = null;

	/* table for histogram */
	Table t;
	static Table time_in_system_table;
	static double lower = 0;
	static double higher = 0;
	double width = 0;

	private class Sim extends com.mesquite.csim.Process {
		public Sim() {
			super("Sim");
		}

		@Override
		public void run() {

			time_in_system_table = new Table("mean time-in-system");
			time_in_system_table.setPermanent(true);
			int i;

			if (type == 1) {
				rundetails();
			} else if (type == 2) {
				for (i = 1; i <= 100; i++) {
					rundetails();
				}
				lower = time_in_system_table.mean() - 2.58 * Math.sqrt(time_in_system_table.var() / 100);
				higher = time_in_system_table.mean() + 2.58 * Math.sqrt(time_in_system_table.var() / 100);
				System.out.println("99% confidence of interval based on 100 times simulation:" + lower + "--" + higher);
			} else if (type == 3) {
				for (i = 1; width == 0 || width >= 0.01 * time_in_system_table.mean(); i++) {
					rundetails();
					lower = time_in_system_table.mean() - 2.58 * Math.sqrt(time_in_system_table.var() / i);
					higher = time_in_system_table.mean() + 2.58 * Math.sqrt(time_in_system_table.var() / i);
					width = higher - lower;
				}
				System.out.println(
						"Compute the mean time-in-system to an accuracy of 1%, with 99% confidence.How many runs did it take to compute this?"
								+ (i - 1));
			} else {
				return;
			}

		}

		public void rundetails() {
			/* initiate all */
			Trainindex = 0;
			depart_time = 0;
			total_in_sys_time = 0;
			max_in_sys_time = 0;
			total_busy_time = 0;
			total_idle_time = 0;
			total_hogged_out_time = 0;
			latest_arr_or_depart_time = 0;
			area = 0;
			max_trains_in_queue = 0;
			f = new FCFSFacility("f", 1);
			l = new LinkedList<Train>();
			e = new LinkedList<Event>();
			depart = new Event("depart");
			depart.set();
			e.add(0, depart);
			/* files read initiation */
			if (inputcase == 2) {
				file1 = new File(System.getProperty("user.dir") + "/" + fname1);
				file2 = new File(System.getProperty("user.dir") + "/" + fname2);
				try {
					r1 = new FileReader(file1);
					r2 = new FileReader(file2);
					buf1 = new BufferedReader(r1);
					buf2 = new BufferedReader(r2);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}
			/* histogram initiate */
			t = new Table("histogram of the num of trains hog out 0,1,2,etc times:");
			t.add_histogram(5, 0, 5);

			add(new Arrive());
			hold(75000);
			System.out.println(System.getProperty("user.dir"));
			System.out.println("Total num of trains served:" + Trainindex);
			System.out.println("Average of the time-in-system over trains:" + total_in_sys_time / Trainindex);
			time_in_system_table.record(total_in_sys_time / Trainindex);
			System.out.println("max of the time-in-system over trains:" + max_in_sys_time);
			System.out.println("percent of time the loading dock spent busy:" + total_busy_time / depart_time);
			System.out.println("percent of time the loading dock spent idle:" + total_idle_time / depart_time);
			System.out.println(
					"percent of time the loading dock spent hogged_out:" + total_hogged_out_time / depart_time);
			System.out.println("max num of trains in queue:" + max_trains_in_queue);
			System.out.println("Time average num of trains in queue:" + area / depart_time);
		}
	}

	private class Arrive extends com.mesquite.csim.Process {

		Train t;
		private double Arrivetime = 0;
		private double Unloadtime = 0;

		public Arrive() {
			super("Arrive");
		}

		@Override
		public void run() {
			do {
				String line = null;
				String line2 = null;
				String arr[] = null;
				/* case 1 */
				if (inputcase == 1) {
					double ArriveInterval = rand.exponential(ave_arr_interval);
					hold(ArriveInterval);
					Arrivetime = Arrivetime + ArriveInterval;
					if (Arrivetime > max_arr_time)
						break;
					Unloadtime = rand.uniform(3.5, 4.5);
					/* case 2 */
				} else if (inputcase == 2) {
					try {
						if (buf1 != null)
							line = buf1.readLine();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					if (line != null) {
						arr = line.split(" ");
						hold(Double.parseDouble(arr[0]) - Arrivetime);
						Arrivetime = Double.parseDouble(arr[0]);
						Unloadtime = Double.parseDouble(arr[1]);
					} else {
						break;
					}
				}
				Trainindex++;
				System.out.println("train " + Trainindex + "arrive at " + Arrivetime);
				area = area + l.size() * (Arrivetime - latest_arr_or_depart_time);
				latest_arr_or_depart_time = Arrivetime;

				t = new Train();
				t.setIndex(Trainindex);
				t.setArrive_time(Arrivetime);
				t.setUnload_time(Unloadtime);
				t.H = new Hog();
				t.H.setHog_out_end_time(Arrivetime);

				/* case 1 */
				if (inputcase == 1) {
					t.H.setRemaining_work_time(rand.uniform(6, 11));
					t.H.setHog_out_time(rand.uniform(2.5, 3.5));
					/* case 2 */
				} else if (inputcase == 2) {
					if (line != null) {
						t.H.setRemaining_work_time(Double.parseDouble(arr[2]));
						if (buf2 != null)
							try {
								line2 = buf2.readLine();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						if (line2 != null)
							t.H.setHog_out_time(Double.parseDouble(line2));
					}
				}
				t.H.setHog_out_start_time(Arrivetime + t.H.getRemaining_work_time());
				t.H.setHasdepart(false);
				t.H.setIndex(Trainindex);
				add(t.H);

				depart = new Event("depart");
				e.add(depart);
				l.add(t);
				add(new Serve());

				if (l.size() >= max_trains_in_queue) {
					max_trains_in_queue = l.size();
				}

			} while (true);
		}
	}

	private class Serve extends com.mesquite.csim.Process {

		double time_btw_depart = 0;

		public Serve() {
			super("Serve");
		}

		public int gettrainbyindex(LinkedList<Train> l, int index) {
			int i;
			for (i = 0; i < l.size(); i++) {
				if (l.get(i).getIndex() == index) {
					break;
				}
			}
			return i;
		}

		@Override
		public void run() {

			Train temptrain = l.get(gettrainbyindex(l, Trainindex));
			e.get(temptrain.getIndex() - 1).untimed_wait();
			if (depart_time < temptrain.getArrive_time()) {
				// System.out.println("depart_time:" + depart_time);
				total_idle_time = total_idle_time + (temptrain.getArrive_time() - depart_time);
				depart_time = temptrain.getArrive_time();
				// System.out.println("arrive_time:" + depart_time);
			}
			/*
			 * case 1: Hog out before unloading and need to wait Hog out end because dock
			 * becomes idle during Hog out
			 */
			if (temptrain.H.getHog_out_start_time() + temptrain.H.getHog_out_time() > depart_time
					& depart_time > temptrain.H.getHog_out_start_time() & temptrain.H.isHog() == true) {
				System.out.println("first  " + "train" + temptrain.getIndex() + "enter dock at "
						+ (temptrain.H.getHog_out_start_time() + temptrain.H.getHog_out_time()));
				time_btw_depart = temptrain.H.getHog_out_start_time() + temptrain.H.getHog_out_time() - depart_time
						+ temptrain.getUnload_time();
				depart_time = temptrain.H.getHog_out_start_time() + temptrain.H.getHog_out_time()
						+ temptrain.getUnload_time();

				total_hogged_out_time = total_hogged_out_time + temptrain.H.getHog_out_start_time()
						+ temptrain.H.getHog_out_time() - depart_time;

				/* case 2: Hog out when unloading */
			} else if (temptrain.H.getHog_out_start_time() < depart_time + temptrain.getUnload_time()
					& temptrain.H.getHog_out_start_time() > depart_time) {
				System.out.println("second   " + "train " + temptrain.getIndex() + "enter dock at" + depart_time);
				time_btw_depart = temptrain.getUnload_time() + temptrain.H.getHog_out_time();
				depart_time = depart_time + temptrain.getUnload_time() + temptrain.H.getHog_out_time();
				System.out.println("hogg time" + temptrain.H.getHog_out_time());

				total_hogged_out_time = total_hogged_out_time + temptrain.H.getHog_out_time();

				/* case 3: enter dock at once or enter the dock as long as next train arrive */
			} else {
				System.out.println("third   " + "train " + temptrain.getIndex() + "enter dock at" + depart_time);
				time_btw_depart = temptrain.getUnload_time();
				depart_time = depart_time + temptrain.getUnload_time();
			}

			f.reserve();
			hold(time_btw_depart);
			f.release();
			/* set the signal to end the Hog process after train departs */
			temptrain.H.setHasdepart(true);

			System.out.println("train " + temptrain.getIndex() + "depart at " + depart_time);
			e.get(temptrain.getIndex()).set();
			if (max_in_sys_time <= depart_time - temptrain.getArrive_time()) {
				max_in_sys_time = depart_time - temptrain.getArrive_time();
			}
			total_in_sys_time = total_in_sys_time + (depart_time - temptrain.getArrive_time());
			total_busy_time = total_busy_time + temptrain.getUnload_time();
			t.record(temptrain.H.getHogtimes());
			area = area + l.size() * (depart_time - latest_arr_or_depart_time);
			latest_arr_or_depart_time = depart_time;

			l.removeFirst();
		}
	}

	public class Train {
		public Hog H;
		private double arrive_time;
		private int index;
		private double unload_time;

		public double getUnload_time() {
			return unload_time;
		}

		public void setUnload_time(double unload_time) {
			this.unload_time = unload_time;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public double getArrive_time() {
			return arrive_time;
		}

		public void setArrive_time(double arrive_time) {
			this.arrive_time = arrive_time;
		}

	}

	private class Hog extends com.mesquite.csim.Process {

		private int index;
		private double Hog_out_start_time;
		private double Hog_out_end_time;
		private double Remaining_work_time;
		private double Hog_out_time;
		boolean hasdepart;
		boolean isHog = false;
		int hogtimes = 0;

		public int getHogtimes() {
			return hogtimes;
		}

		public boolean isHog() {
			return isHog;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public void setHog(boolean isHog) {
			this.isHog = isHog;
		}

		public boolean isHasdepart() {
			return hasdepart;
		}

		public void setHasdepart(boolean hasdepart) {
			this.hasdepart = hasdepart;
		}

		public double getHog_out_end_time() {
			return Hog_out_end_time;
		}

		public void setHog_out_end_time(double hog_out_end_time) {
			Hog_out_end_time = hog_out_end_time;
		}

		public double getHog_out_start_time() {
			return Hog_out_start_time;
		}

		public void setHog_out_start_time(double hog_out_start_time) {
			Hog_out_start_time = hog_out_start_time;
		}

		public double getRemaining_work_time() {
			return Remaining_work_time;
		}

		public void setRemaining_work_time(double remaining_work_time) {
			Remaining_work_time = remaining_work_time;
		}

		public double getHog_out_time() {
			return Hog_out_time;
		}

		public void setHog_out_time(double hog_out_time) {
			Hog_out_time = hog_out_time;
		}

		public Hog() {
			super("Hog");
		}

		@Override
		public void run() {

			while (true) {
				hold(Remaining_work_time);
				if (hasdepart == true) {
					break;
				}
				Hog_out_start_time = Hog_out_end_time + Remaining_work_time;
				System.out.println("train " + index + "hog out at" + Hog_out_start_time);
				isHog = true;

				hold(Hog_out_time);
				Hog_out_end_time = Hog_out_start_time + Hog_out_time;
				isHog = false;
				hogtimes++;
				System.out.println("train " + index + "hog out end at" + Hog_out_end_time);

				/* case 1 */
				if (inputcase == 1) {
					Hog_out_time = rand.uniform(2.5, 3.5);
					/* case 2 */
				} else if (inputcase == 2) {
					try {
						String line = null;
						if (buf2 != null)
							line = buf2.readLine();
						if (line != null)
							Hog_out_time = Double.parseDouble(line);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				Remaining_work_time = 12 - Hog_out_time;
			}
		}

	}

}
