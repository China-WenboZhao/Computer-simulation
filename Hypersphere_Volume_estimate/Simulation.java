import java.util.ArrayList;

public class Simulation {

	static double volume;

	public Simulation() {

	}

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		Monte_Carlo_integration mci = new Monte_Carlo_integration(5, 1000000);
		double temp = mci.CalculateVolume();
		System.out.println(temp);
		System.out.println(temp-5.26379);
		System.out.println("Monte Carlo method");
		System.out.println("total point:" + 1000000);
//		 Cube_based_Integration cbi = new Cube_based_Integration(3,100);
//		 cbi.CalculateVolume();
		
		System.out.println("time:" + (System.currentTimeMillis() - start));
	}
}

