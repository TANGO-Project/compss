package noEmptyConstructor;

import customObjectClasses.InvalidObject;
import customObjectClasses.ValidObject;
import integratedtoolkit.types.annotations.Method;
import integratedtoolkit.types.annotations.Parameter;
import integratedtoolkit.types.annotations.Parameter.Direction;
import integratedtoolkit.types.annotations.Parameter.Type;


public interface MainItf {
    
    @Method(declaringClass = "noEmptyConstructor.MainImpl")
    ValidObject validTask(
        @Parameter(type = Type.OBJECT, direction = Direction.IN) ValidObject voIN,
        @Parameter(type = Type.OBJECT, direction = Direction.INOUT) ValidObject voINOUT
    );
    
    @Method(declaringClass = "noEmptyConstructor.MainImpl")
    InvalidObject invalidTask(
        @Parameter(type = Type.OBJECT, direction = Direction.IN) InvalidObject invoIN,
        @Parameter(type = Type.OBJECT, direction = Direction.INOUT) InvalidObject invoINOUT
    );

}
