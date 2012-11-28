/**
 * 
 */
package com.github.podd.api.purl;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.util.AbstractServiceLoader;

import com.github.podd.api.PoddProcessorStage;

/**
 * A registry for implementations of the PoddPurlProcessorFactory interface.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddPurlProcessorFactoryRegistry extends AbstractServiceLoader<String, PoddPurlProcessorFactory>
{
    
    private static final PoddPurlProcessorFactoryRegistry instance = new PoddPurlProcessorFactoryRegistry();
    
    public PoddPurlProcessorFactoryRegistry()
    {
        super(PoddPurlProcessorFactory.class);
    }
    
    /**
     * @return A static instance of this registry.
     */
    public static PoddPurlProcessorFactoryRegistry getInstance()
    {
        return PoddPurlProcessorFactoryRegistry.instance;
    }
    
    /**
     * From amongst all the PODD PURL processor factories available with this registry, retrieve a
     * list of the factories that support the given <code>PoddProcessorStage</code>.
     * 
     * @param nextStage
     * @return
     */
    public final List<PoddPurlProcessorFactory> getByStage(final PoddProcessorStage nextStage)
    {
        final List<PoddPurlProcessorFactory> result = new ArrayList<PoddPurlProcessorFactory>();
        for(final PoddPurlProcessorFactory nextProcessor : this.getAll())
        {
            if(nextProcessor.canHandleStage(nextStage))
            {
                result.add(nextProcessor);
            }
        }
        
        return result;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.semanticweb.owlapi.util.AbstractServiceLoader#getKey(java.lang.Object)
     */
    @Override
    protected final String getKey(final PoddPurlProcessorFactory nextFactory)
    {
        return nextFactory.getKey();
    }
    
}
