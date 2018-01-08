import java.util.ArrayList;
import java.util.Random;

public class Cube_based_Integration {

	ArrayList<Double> SCube_Center_Coordinate;
	int dimension;
	double segment;
	double hypercube_volume;
	double in_hypersphere_Scube = 0;
	// double cross_hypersphere_Scube = 0;
	double pass_hypersphere_Scube = 0;
	double out_hypersphere_Scube = 0;
	double total_hypersphere_Scube = 0;
	double UpperBounder = 0.0;
	double LowerBounder = 0.0;

	public double getUpperBounder() {
		return UpperBounder;
	}

	public double getLowerBounder() {
		return LowerBounder;
	}

	public Cube_based_Integration(int dimension, double segment) {
		this.dimension = dimension;
		this.segment = segment;
		hypercube_volume = Math.pow(2, dimension);
		total_hypersphere_Scube = Math.pow(segment, dimension)/Math.pow(2, dimension);
		SCube_Center_Coordinate = new ArrayList<Double>();
		for (int i = 0; i <= dimension; i++) {
			SCube_Center_Coordinate.add(1.0 / segment);
		}
	}

	public void CalculateVolume() {
		// set a redundant bit, which is used to check if has finished loop
		int lastindex = SCube_Center_Coordinate.size() - 1;
		judgeSmallCubeLocation();
		//calculate_in_out_cube();
		do {
			double temp = SCube_Center_Coordinate.get(0) + 2.0 / segment;
			SCube_Center_Coordinate.set(0, temp);
			for (int i = 0; i < dimension; i++) {
				if (SCube_Center_Coordinate.get(i) >= 1.0) {
					SCube_Center_Coordinate.set(i, 1.0 / segment);
					temp = SCube_Center_Coordinate.get(i + 1) + 2.0 / segment;
					SCube_Center_Coordinate.set(i + 1, temp);
				}
			}
			if (SCube_Center_Coordinate.get(lastindex) < 3.0 / segment) {
				judgeSmallCubeLocation();
				//calculate_in_out_cube();
			}
		} while (SCube_Center_Coordinate.get(lastindex) < 3.0 / segment);

		UpperBounder = (total_hypersphere_Scube - out_hypersphere_Scube) / total_hypersphere_Scube * hypercube_volume;
		LowerBounder = in_hypersphere_Scube / total_hypersphere_Scube * hypercube_volume;

		System.out.println("dimension:" + dimension + " segment per dimension:" + segment);
		System.out.println("two-dot-method");
		double temp= (in_hypersphere_Scube +1/2*pass_hypersphere_Scube) / total_hypersphere_Scube * hypercube_volume;
		System.out.println(temp);
		System.out.println(temp-4.18879);
//		System.out.println(UpperBounder-LowerBounder);
//		System.out.println(LowerBounder + "~" + UpperBounder);

	}

	public void calculate_in_out_cube() {
		double tempdouble = 0.0;
		double tempdouble2 = 0.0;
		double sum = 0.0;
		double sum2 = 0.0;
		double smallHypersphereR = 0.0;
		for (int j = 0; j < dimension; j++) {
			tempdouble = Math.abs(SCube_Center_Coordinate.get(j) - 1) - (1.0 / segment);
			tempdouble2 = Math.pow((SCube_Center_Coordinate.get(j) - 1), 2);
			// System.out.println(tempdouble);
			if (tempdouble < 0) {
				tempdouble = 0;
			}
			sum = sum + Math.pow(tempdouble, 2);
			sum2 = sum2 + tempdouble2;

		}
		if (sum > 1.0 * 1.0) {
			out_hypersphere_Scube++;
		}

		sum2 = Math.sqrt(sum2);
		smallHypersphereR = Math.sqrt(dimension) / segment;
		if (sum2 + smallHypersphereR <= 1) {
			in_hypersphere_Scube++;
		}
	}

	public void judgeSmallCubeLocation() {
		ArrayList<Double> tempCoordinate = new ArrayList<Double>();
		ArrayList<Double> tempCoordinate2 = new ArrayList<Double>();
		double sum = 0.0;
		double sum2 = 0.0;
		for (int j = 0; j < dimension; j++) {
			if ((SCube_Center_Coordinate.get(j) - 1) <= 0) {
				tempCoordinate.add(SCube_Center_Coordinate.get(j) - (1.0 / segment));
				tempCoordinate2.add(SCube_Center_Coordinate.get(j) + (1.0 / segment));

			} else {
				 tempCoordinate.add(SCube_Center_Coordinate.get(j) + (1.0 / segment));
				 tempCoordinate2.add(SCube_Center_Coordinate.get(j) - (1.0 / segment));
				
			}
			 sum = sum + Math.pow(tempCoordinate.get(j) - 1, 2);
			 sum2 = sum2 + Math.pow(tempCoordinate2.get(j) - 1, 2);
		}
		if (sum > 0) {
			if (sum <= 1 * 1) {
				in_hypersphere_Scube++;
			} else if (sum > 1 * 1) {
				if (sum2 < 1 * 1) {
					pass_hypersphere_Scube++;
				} else if (sum2 >= 1 * 1) {
					out_hypersphere_Scube++;
				}
			}
		}
	}
}
