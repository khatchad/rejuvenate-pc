package p;

public class HybridAutomobile {
	
	private double overallSpeed; 
	//...
	//Sets the new speed for changes in fuel.
	public void notifyChangeIn(Fuel fuel) {
		this.overallSpeed += 
			fuel.calculateDelta(this); 
		/* Update attached observers ... */
	}
	
	//Sets the new speed for changes in electricity.
	public void notifyChangeIn(Current current) {
		this.overallSpeed += 
			current.calculateDelta(this); 
		/* Update attached observers ... */
	}

	//Sets the new speed directly.
	public void notifyChangeIn(double mph) {
		this.overallSpeed += mph;
		/* Update attached observers ... */
	}

	public double getOverallSpeed() {
		return overallSpeed;
	}
}
