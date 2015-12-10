package gridMethod;

public class ZeroFlux extends ConstantNeumann implements GridMethod {

	// TODO not to sure about this one, originally this was just returning
	// constantNeumann, we may leave this out, synonyms may cause confusion.
	ZeroFlux(double gradient) {
		super(gradient);
	}

}
