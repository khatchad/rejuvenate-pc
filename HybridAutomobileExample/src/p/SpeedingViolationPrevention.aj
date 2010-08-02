package p;

public aspect SpeedingViolationPrevention {
	void around(CarComponent component, Energy energy) : 
		execution(void increase(Energy+)) && this(component) && args(energy) {

		// get the current speed limit
		double speedLimit = Highway.getSpeedLimit();

		// get the car's current speed.
		HybridAutomobile car = component.getCar();
		double currentSpeed = car.getOverallSpeed();
		
		//get the amount to add.
		double changeInMPH = energy.calculateDelta(car);
		
		double newSpeed = currentSpeed + changeInMPH;
		
		if ( newSpeed <= speedLimit )
			proceed(component, energy);
	}

	// execution(void increase(Energy+)) || execution(void increase(double)){
}
