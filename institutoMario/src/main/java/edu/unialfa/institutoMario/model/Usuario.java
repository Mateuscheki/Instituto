package edu.unialfa.institutoMario.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Data
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senha;

    @ManyToOne
    @JoinColumn(name = "id_tipo_usuario")
    @ToString.Exclude
    @JsonManagedReference
    private TipoUsuario tipoUsuario;

    private String nome;

    private String telefone;

    @Column(unique = true)
    private String cpf;

    @Column(unique = true)
    private String email;

    // ===== CAMPOS PARA RECUPERAÇÃO DE SENHA =====

    private String resetPasswordToken;

    private LocalDateTime resetPasswordTokenExpiry;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleBase = switch (tipoUsuario.getId().intValue()) {
            case 1 -> "ROLE_ADMIN";
            case 2 -> "ROLE_PROFESSOR";
            default -> "ROLE_ALUNO";
        };

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(roleBase));

        // O módulo Benefícios usa perfis próprios (ADM/GESTAO/ATENDIMENTO —
        // ver CLAUDE.md do módulo). Nesta primeira etapa ainda não existe um
        // cadastro de perfis dedicado a Benefícios, então o Administrador do
        // sistema (tipoUsuario=1) também assume o papel de ADM do módulo.
        if ("ROLE_ADMIN".equals(roleBase)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADM"));
        }

        return authorities;
    }

    @Override
    public String getUsername() {
        return this.id.toString();
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}