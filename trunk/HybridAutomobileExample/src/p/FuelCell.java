package p;

public class FuelCell {

	private HybridAutomobile car;

	public void increase(double mph) {
		// ...
		this.car.notifyChangeIn(mph);
	}

}
