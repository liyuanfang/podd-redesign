/**
 * 
 */
package com.github.podd.utils;

import org.openrdf.model.URI;

/**
 * This class represents a PODD User.
 * 
 * A User is always created in the INACTIVE state and should be activated when necessary by calling
 * the <code>activate()</code> method. By default, users have NULL set as their password. It is
 * necessary to activate a user before a password can be set.
 * 
 * An email address is mandatory at user-creation and it cannot be modified thereafter.
 * 
 * 
 * @author kutila
 * 
 */
public abstract class AbstractPoddUser implements PoddUser
{
    
    private PoddUserStatus status = PoddUserStatus.INACTIVE;
    
    /**
     * An email address that uniquely identifies a PODD User.
     */
    private String email = null;
    
    /**
     * Unique URI that is used to identify and refer to a PODD user. The URI can be set once and
     * cannot be modified thereafter.
     */
    private URI uri = null;
    
    // optional parameters of a PODD User
    private String firstName = null;
    private String lastName = null;
    private String orcidID = null;
    private String organization = null;
    private char[] secret = null;
    
    /**
     * Create a PODD user by providing only an email address. The created user is in the INACTIVE
     * state by default.
     * 
     * @param email
     */
    public AbstractPoddUser(final String email)
    {
        this.email = email;
    }
    
    /**
     * Create a PODD User by passing in multiple parameters. The created user is in the INACTIVE
     * state by default.
     * 
     * @param email
     * @param firstname
     * @param lastname
     * @param orcidid
     * @param organization
     */
    public AbstractPoddUser(final String email, final String firstname, final String lastname, final String orcidid,
            final String organization)
    {
        this.email = email;
        this.firstName = firstname;
        this.lastName = lastname;
        this.orcidID = orcidid;
        this.organization = organization;
    }
    
    /* (non-Javadoc)
     * @see com.github.podd.utils.PoddUser#setUri(org.openrdf.model.URI)
     */
    @Override
    public void setUri(final URI uri)
    {
        if(this.uri == null)
        {
            this.uri = uri;
        }
    }
    
    /* (non-Javadoc)
     * @see com.github.podd.utils.PoddUser#getUri()
     */
    @Override
    public URI getUri()
    {
        return this.uri;
    }
    
    /* (non-Javadoc)
     * @see com.github.podd.utils.PoddUser#getEmail()
     */
    @Override
    public String getEmail()
    {
        return this.email;
    }
    
    /* (non-Javadoc)
     * @see com.github.podd.utils.PoddUser#getSecret()
     */
    @Override
    public char[] getSecret()
    {
        return this.secret;
    }
    
    /* (non-Javadoc)
     * @see com.github.podd.utils.PoddUser#setSecret(char[])
     */
    @Override
    public void setSecret(final char[] secret)
    {
            this.secret = secret;
    }
    
    /* (non-Javadoc)
     * @see com.github.podd.utils.PoddUser#getFirstName()
     */
    @Override
    public String getFirstName()
    {
        return this.firstName;
    }
    
    /* (non-Javadoc)
     * @see com.github.podd.utils.PoddUser#setFirstName(java.lang.String)
     */
    @Override
    public void setFirstName(final String firstName)
    {
        this.firstName = firstName;
    }
    
    /* (non-Javadoc)
     * @see com.github.podd.utils.PoddUser#getLastName()
     */
    @Override
    public String getLastName()
    {
        return this.lastName;
    }
    
    /* (non-Javadoc)
     * @see com.github.podd.utils.PoddUser#setLastName(java.lang.String)
     */
    @Override
    public void setLastName(final String lastName)
    {
        this.lastName = lastName;
    }
    
    /* (non-Javadoc)
     * @see com.github.podd.utils.PoddUser#getOrcid()
     */
    @Override
    public String getOrcid()
    {
        return this.orcidID;
    }
    
    /* (non-Javadoc)
     * @see com.github.podd.utils.PoddUser#setOrcid(java.lang.String)
     */
    @Override
    public void setOrcid(final String orcid)
    {
        this.orcidID = orcid;
    }
    
    /* (non-Javadoc)
     * @see com.github.podd.utils.PoddUser#getOrganization()
     */
    @Override
    public String getOrganization()
    {
        return this.organization;
    }
    
    /* (non-Javadoc)
     * @see com.github.podd.utils.PoddUser#setOrganization(java.lang.String)
     */
    @Override
    public void setOrganization(final String organization)
    {
        this.organization = organization;
    }

    /* (non-Javadoc)
     * @see com.github.podd.utils.PoddUser#getStatus()
     */
    @Override
    public PoddUserStatus getStatus()
    {
        return status;
    }

    /* (non-Javadoc)
     * @see com.github.podd.utils.PoddUser#setStatus(com.github.podd.utils.USER_STATUS)
     */
    @Override
    public void setStatus(PoddUserStatus status)
    {
        this.status = status;
    }
    
    
    
}
