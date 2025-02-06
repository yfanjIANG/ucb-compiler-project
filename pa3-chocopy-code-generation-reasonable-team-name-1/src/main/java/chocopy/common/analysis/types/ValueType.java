package chocopy.common.analysis.types;

import chocopy.common.astnodes.ClassType;
import chocopy.common.astnodes.ListType;
import chocopy.common.astnodes.StudentType;
import chocopy.common.astnodes.TypeAnnotation;

/**
 * A ValueType references types that are assigned to variables and
 * expressions.
 *
 * In particular, ValueType can be a {@link ClassValueType} (e.g. "int") or
 * a {@link ListValueType} (e.g. "[int]").
 */

public abstract class ValueType extends Type {

    /** Returns the type corresponding to ANNOTATION. */
    public static ValueType annotationToValueType(TypeAnnotation annotation) {
        return switch (annotation) {
            case ClassType classAnnot-> new ClassValueType(classAnnot);
            case ListType listAnnot -> new ListValueType(listAnnot);
            case StudentType studentAnnot -> {
                String className = studentAnnot.getClass().getCanonicalName();
                throw new RuntimeException(
                    "case not handled for class of type " + className
                );
            }
        };
    }

    @Override
    public boolean isValueType() {
        return true;
    }

}
