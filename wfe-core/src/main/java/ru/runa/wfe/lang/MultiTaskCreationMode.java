package ru.runa.wfe.lang;

/**
 * Defines mode type for creating tasks in multitask node.
 */
public enum MultiTaskCreationMode {
    /**
     * Creates task for each element in list and assign tasks for list element
     * value.
     */
    BY_EXECUTORS,

    /**
     * Creates tasks for each element in list and assign tasks for single
     * swimlane actor. List element value is accessible in task via variables.
     */
    BY_DISCRIMINATOR
}
