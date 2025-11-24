/**
 * Utilities for detecting external locks on affinity resources.
 *
 * <p>These classes guard against multiple processes competing for the same CPU
 * bindings by coordinating via file locks and related checks.
 */
package net.openhft.affinity.lockchecker;
