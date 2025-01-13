package com.appiancorp.solutionsconsulting.plugin.mongodb;

import com.appiancorp.suiteapi.type.TypeService;
import com.appiancorp.suiteapi.type.TypedValue;
import com.appiancorp.type.AppianTypeLong;

import java.util.*;


/**
 * Provides helper methods for working with Appian {@link TypedValue} objects, particularly
 * for checking whether they represent dictionaries/CDTs or lists of dictionaries/CDTs.
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class AppianTypeHelper {
    /**
     * Determines whether a {@link TypedValue} can be interpreted as either a list of dictionaries/CDTs
     * or a single dictionary/CDT. It attempts to cast to a list of dictionaries first, and if that fails,
     * tries to cast to a single dictionary.
     *
     * @param typeService the Appian {@link TypeService} used for casting
     * @param typedValue  the {@link TypedValue} to examine
     * @return {@code true} if the value is a dictionary/CDT (list or single), otherwise {@code false}
     */
    public static Boolean isListDictOrCdt(TypeService typeService, TypedValue typedValue) {
        if (typedValue == null) return false;
        try {
            Object castVal = typeService.cast(AppianTypeLong.LIST_OF_DICTIONARY, typedValue).getValue();
            if (castVal instanceof HashMap[]) {
                // Now we know it's safe to cast
                @SuppressWarnings("unchecked")
                HashMap<TypedValue, TypedValue>[] arr = (HashMap<TypedValue, TypedValue>[]) castVal;
                new ArrayList<>(Arrays.asList(arr));
            } else {
                throw new IllegalStateException("Expected LIST_OF_DICTIONARY but got a different type.");
            }
        } catch (Exception e1) {
            try {
                typeService.cast(AppianTypeLong.DICTIONARY, typedValue).getValue();
            } catch (Exception e2) {
                return false;
            }
        }
        return true;
    }
}
