package p;

public class DieselEngine implements CarComponent {
	
	private HybridAutomobile car;
	
	public HybridAutomobile getCar() {
		return car;
	}

	public void increase(Fuel fuel) { 
		//...
		this.car.notifyChangeIn(fuel);
	}
}
