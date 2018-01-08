
import java.util.ArrayList;

public class Monte_Carlo_integration {

	ArrayList<Double> Coordinate;
	int dimension;
	double testing_num;
	double hypercube_volume;
	// Random r;

	public Monte_Carlo_integration(int dimension, double testing_num) {
		this.dimension = dimension;
		this.testing_num = testing_num;
		hypercube_volume = Math.pow(2, dimension);

	}

	public double CalculateVolume() {
		double point_in_hypersphere = 0;
		for (int i = 0; i < testing_num; i++) {

			// GenCoordinate();
			Coordinate = new ArrayList<Double>();
			for (int j = 0; j < dimension; j++) {
				double tempDouble = Math.random() * 2.0;
				// System.out.println("tempdouble"+tempDouble);
				Coordinate.add(tempDouble);
			}

			double sum = 0.0;
			for (int j = 0; j < dimension; j++) {
				// System.out.println(Coordinate.get(j));
				sum = sum + Math.pow(Coordinate.get(j) - 1, 2);
			}
			if (sum <= 1 * 1) {
				point_in_hypersphere++;
			}
		}
		double volume = point_in_hypersphere / testing_num * hypercube_volume;
		return volume;
	}

	public void multiruns() {

		int round = 15;
		double lower = 0.0;
		double higher = 0.0;

	//	do {
			round = round + 10;
			double mean = 0.0;
			double variance = 0.0;
			double sum = 0.0;
			double squaresum = 0.0;
			double temp = 0.0;
			double accuracy =0;
			ArrayList<Double> volume = new ArrayList<Double>();

			for (int i = 0; i < round; i++) {

				temp = CalculateVolume();
				volume.add(temp);
				sum = sum + temp;
			}
			mean = sum / round;
			for (int i = 0; i < round; i++) {
				temp = volume.get(i);
				squaresum = squaresum + Math.pow((temp - mean), 2);
			}
			variance = squaresum / (round - 1);
			lower = mean - Math.sqrt(variance / round);
			higher = mean + Math.sqrt(variance / round);
			System.out.println(lower + "~" + higher);
			System.out.println(higher-lower);
			
			lower = Math.floor(lower * 10000) / 10000;
			higher = Math.floor(higher * 10000) / 10000;
			
			//System.out.println(lower + "~" + higher);

			// if (lower == higher) {
			// System.out.println("is correct to 4 digits with 99% confidence? :" + true);
			// } else {
			// System.out.println("is correct to 4 digits with 99% confidence? :" + false);
			// }
	//	} while (lower != higher || lower != 3.1415);

		System.out.println("monte_carlo method");
		System.out.println("total point:" + testing_num * round);

	}

}
