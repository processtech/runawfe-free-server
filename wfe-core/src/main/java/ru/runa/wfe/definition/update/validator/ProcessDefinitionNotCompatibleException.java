package ru.runa.wfe.definition.update.validator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.runa.wfe.InternalApplicationException;

/**
 * @author azyablin
 */
@RequiredArgsConstructor
@Getter
@ToString
public class ProcessDefinitionNotCompatibleException extends InternalApplicationException {
    public static final String NODE_EXISTENCE = "node.existence";
    public static final String PARALLEL_GATEWAY_MISTYPED = "parallel.gateway.mistyped";
    public static final String PARALLEL_GATEWAY_TRANSITIONS = "parallel.gateway.transitions";
    public static final String PARALLEL_GATEWAY_MISSED = "parallel.gateway.missed";
    public static final String PARALLEL_GATEWAY_FROZEN = "parallel.gateway.frozen";
    private static final long serialVersionUID = 1L;
    private final String type;
    private final String[] args;

}
