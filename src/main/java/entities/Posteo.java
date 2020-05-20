/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import commons.DalEntity;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author lorenzofabro
 */
@Entity
@Table(name = "POSTEO")
@NamedQueries({
    @NamedQuery(name = "Posteo.findAll", query = "SELECT p FROM Posteo p"),
    @NamedQuery(name = "Posteo.findByIdPalabra", query = "SELECT p FROM Posteo p WHERE p.posteoPK.idPalabra = :idPalabra"),
    @NamedQuery(name = "Posteo.findByIdDocumento", query = "SELECT p FROM Posteo p WHERE p.posteoPK.idDocumento = :idDocumento"),
    @NamedQuery(name = "Posteo.findByIdPalabraAndIdDocumento", query = "SELECT p FROM Posteo p WHERE p.posteoPK.idPalabra = :idPalabra AND p.posteoPK.idDocumento = :idDocumento"),
    @NamedQuery(name = "Posteo.findByFrecuencia", query = "SELECT p FROM Posteo p WHERE p.frecuencia = :frecuencia")})
public class Posteo implements DalEntity {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected PosteoPK posteoPK;
    @Column(name = "FRECUENCIA")
    private Integer frecuencia;
    @JoinColumn(name = "ID_DOCUMENTO", referencedColumnName = "ID", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Documento documento;
    @JoinColumn(name = "ID_PALABRA", referencedColumnName = "ID", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Palabra palabra;

    public Posteo() {
    }

    public Posteo(PosteoPK posteoPK) {
        this.posteoPK = posteoPK;
    }

    public Posteo(long idPalabra, long idDocumento) {
        this.posteoPK = new PosteoPK(idPalabra, idDocumento);
    }
    
    public Posteo(Palabra palabra, Documento documento, int frecuencia) {
        this.posteoPK = new PosteoPK(palabra.getId(), documento.getId());
        this.palabra = palabra;
        this.documento = documento;
        this.frecuencia = frecuencia;
    }

    public PosteoPK getPosteoPK() {
        return posteoPK;
    }

    public void setPosteoPK(PosteoPK posteoPK) {
        this.posteoPK = posteoPK;
    }

    public Integer getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(Integer frecuencia) {
        this.frecuencia = frecuencia;
    }

    public Documento getDocumento() {
        return documento;
    }

    public void setDocumento(Documento documento) {
        this.documento = documento;
    }

    public Palabra getPalabra() {
        return palabra;
    }

    public void setPalabra(Palabra palabra) {
        this.palabra = palabra;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (posteoPK != null ? posteoPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Posteo)) {
            return false;
        }
        Posteo other = (Posteo) object;
        if ((this.posteoPK == null && other.posteoPK != null) || (this.posteoPK != null && !this.posteoPK.equals(other.posteoPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Posteo{"
                + "Documento=" + this.documento.toString() + ", "
                + "frecuencia=" + this.frecuencia
                + "}";
    }

}
