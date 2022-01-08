package com.ferreusveritas.dynamictrees.api;

import java.lang.annotation.*;

/**
 * Indicates that the target type handles generating fruit.
 *
 * @author Harley O'Connor
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GeneratesFruit {

}
