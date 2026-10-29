package toshibaac.client.types

/**
 * Defrost bits of the FCU state. The AC accepts them only in heat or auto mode, and only models with defrost
 * support react to them. Both bits are always sent together, so set [strong] to the current value when requesting
 * [onDemand] defrost.
 */
public data class Defrost(
    /** Persistent strong defrost setting. */
    public val strong: Boolean,
    /** One-shot request to defrost now. */
    public val onDemand: Boolean,
)
