package integratedtoolkit.types.resources;

import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Implementation.Type;


public class ServiceResourceDescription extends WorkerResourceDescription {

    private final String serviceName;
    private final String namespace;
    private final String port;

    
    public ServiceResourceDescription(String serviceName, String namespace, String port) {
        this.serviceName = serviceName;
        this.namespace = namespace;
        this.port = port;
    }

    public String getServiceName() {
		return serviceName;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getPort() {
		return port;
	}


    public void hosts(Implementation<?> impl) {
        //Do nothing
    }

	@Override
    public boolean canHost(Implementation<?> impl) {
        if (impl.getType() == Type.SERVICE) {
            ServiceResourceDescription s = (ServiceResourceDescription) impl.getRequirements();
            return s.getServiceName().compareTo(serviceName) == 0
                    && s.getNamespace().compareTo(namespace) == 0
                    && s.getPort().compareTo(port) == 0;
        }
        return false;
    }

    @Override
    public void increase(ResourceDescription rd) {

    }

    @Override
    public void increaseDynamic(ResourceDescription rd) {

    }

    @Override
    public void reduce(ResourceDescription rd) {

    }

    @Override
    public ResourceDescription reduceDynamic(ResourceDescription rd) {
    	// Nothing to reduce
    	return null;
    }

    @Override
    public ResourceDescription getDynamicCommons(ResourceDescription constraints) {
        return new ServiceResourceDescription("", "", "");
    }

    @Override
    public boolean isDynamicUseless() {
        return false;
    }

    public String toString() {
        return "[SERVICE "
                + "NAMESPACE=" + this.namespace + " "
                + "SERVICE_NAME=" + this.getServiceName() + " "
                + "PORT=" + this.port
                + "]";
    }

    @Override
    public ServiceResourceDescription copy() {
        return new ServiceResourceDescription(serviceName, namespace, port);
    }

}
