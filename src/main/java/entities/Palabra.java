/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import commons.DalEntity;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author lorenzofabro
 */
@Entity
@Table(name = "PALABRA")
@NamedQueries({
    @NamedQuery(name = "Palabra.findAll", query = "SELECT p FROM Palabra p"),
    @NamedQuery(name = "Palabra.findById", query = "SELECT p FROM Palabra p WHERE p.id = :id"),
    @NamedQuery(name = "Palabra.findByNombre", query = "SELECT p FROM Palabra p WHERE p.nombre = :nombre"),
    @NamedQuery(name = "Palabra.findByCantDocumentos", query = "SELECT p FROM Palabra p WHERE p.cantDocumentos = :cantDocumentos"),
    @NamedQuery(name = "Palabra.findByMaxFrecuencia", query = "SELECT p FROM Palabra p WHERE p.maxFrecuencia = :maxFrecuencia")})
public class Palabra implements DalEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "NOMBRE")
    private String nombre;
    @Column(name = "CANT_DOCUMENTOS")
    private Integer cantDocumentos;
    @Column(name = "MAX_FRECUENCIA")
    private Integer maxFrecuencia;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "palabra")
    private Collection<Posteo> posteoCollection;

    public Palabra() {
    }

    public Palabra(Long id) {
        this.id = id;
    }

    public Palabra(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
    
    public Palabra(String nombre, int cantDocumentos, int maxFrecuencia) {
        this.nombre = nombre;
        this.cantDocumentos = cantDocumentos;
        this.maxFrecuencia = maxFrecuencia;
        this.posteoCollection = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getCantDocumentos() {
        return cantDocumentos;
    }

    public void setCantDocumentos(Integer cantDocumentos) {
        this.cantDocumentos = cantDocumentos;
    }

    public Integer getMaxFrecuencia() {
        return maxFrecuencia;
    }

    public void setMaxFrecuencia(Integer maxFrecuencia) {
        this.maxFrecuencia = maxFrecuencia;
    }

    public Collection<Posteo> getPosteoCollection() {
        return posteoCollection;
    }

    public void setPosteoCollection(Collection<Posteo> posteoCollection) {
        this.posteoCollection = posteoCollection;
    }
    
    public void addCantDocumentos() {
        this.cantDocumentos++;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Palabra)) {
            return false;
        }
        Palabra other = (Palabra) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Palabra{" +
                "id=" + this.id + ", " +
                "nombre='" + this.nombre + "', " +
                "cantDocumentos=" + this.cantDocumentos + ", " +
                "maxFrecuencia=" + this.maxFrecuencia + ", " +
                "posteos=" + this.posteoCollection.toString() + "," +
                "}";
    }
    
}
