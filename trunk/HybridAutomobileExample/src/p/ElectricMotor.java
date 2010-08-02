package p;

public class ElectricMotor implements CarComponent {
	
	private HybridAutomobile car;
	
	public void increase(Current current) { 
		//...
		this.getCar().notifyChangeIn(current);
	}

	public HybridAutomobile getCar() {
		return car;
	}
}
