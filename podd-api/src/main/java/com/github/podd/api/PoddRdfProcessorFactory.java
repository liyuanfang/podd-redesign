/**
 * 
 */
package com.github.podd.api;

import org.openrdf.model.Graph;

/**
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 *
 * @param <T> The type of objects that this RDF Processor creates.
 */
public interface PoddRdfProcessorFactory<T> extends PoddProcessorFactory<PoddRdfProcessor<T>, PoddRdfProcessorEvent<T>, Graph, T>
{
    
}
