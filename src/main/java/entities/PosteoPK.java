/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 *
 * @author lorenzofabro
 */
@Embeddable
public class PosteoPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "ID_PALABRA")
    private long idPalabra;
    @Basic(optional = false)
    @NotNull
    @Column(name = "ID_DOCUMENTO")
    private long idDocumento;

    public PosteoPK() {
    }

    public PosteoPK(long idPalabra, long idDocumento) {
        this.idPalabra = idPalabra;
        this.idDocumento = idDocumento;
    }

    public long getIdPalabra() {
        return idPalabra;
    }

    public void setIdPalabra(long idPalabra) {
        this.idPalabra = idPalabra;
    }

    public long getIdDocumento() {
        return idDocumento;
    }

    public void setIdDocumento(long idDocumento) {
        this.idDocumento = idDocumento;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) idPalabra;
        hash += (int) idDocumento;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PosteoPK)) {
            return false;
        }
        PosteoPK other = (PosteoPK) object;
        if (this.idPalabra != other.idPalabra) {
            return false;
        }
        if (this.idDocumento != other.idDocumento) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.PosteoPK[ idPalabra=" + idPalabra + ", idDocumento=" + idDocumento + " ]";
    }
    
}
