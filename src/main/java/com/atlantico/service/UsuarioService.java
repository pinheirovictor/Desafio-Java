package com.atlantico.service;

import com.atlantico.model.Usuario;
import com.atlantico.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
    @Autowired
    PasswordEncoder encoder;

    // metodo buscar por email
    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // metodo listar todos
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    // metodo de salvar usuário
    public Usuario save(Usuario usuario) {
        if(findByEmail(usuario.getEmail()) == null) {
            usuario.setPassword(encoder.encode(usuario.getPassword()));
            return usuarioRepository.save(usuario);
        }else{
            throw new DataIntegrityViolationException("Usuário de Email já cadastrado");
        }
    }

    // metodo deletar usuario
    public void delete(Long id){
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        Object usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(usuario!=null && !usuario.getEmail().equals(((UserDetails) usuarioLogado).getUsername())){
            throw new DataIntegrityViolationException("Não é possível excluir o usuário logado");
        }
        try {
            if(usuario!=null)
                usuarioRepository.delete(usuario);
        }catch (DataIntegrityViolationException e){
            throw new DataIntegrityViolationException("Não é possível excluir usuário");
        }
    }

    // metodo de atualizar senha
    public Usuario updatePassword(String password, String newPassword){
        Object usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = ( (UserDetails)usuarioLogado).getUsername();
        Usuario usuario = usuarioRepository.findByEmail(email);
        if(usuario.isAdmin() && encoder.matches(password, usuario.getPassword())){
            usuario.setPassword(encoder.encode(newPassword));
            return usuarioRepository.save(usuario);
        }else{
            throw new DataIntegrityViolationException("Usuário não é admin pra trocar a senha");
        }
    }

    // metodo de pegar usuario logado
    public Usuario getUsuarioLogado(){
        Object usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = ( (UserDetails)usuarioLogado).getUsername();
        return usuarioRepository.findByEmail(email);
    }

    // metodo de verificar usuario
    public boolean verificaUsuario(Usuario usuario){
        return usuario.equals(getUsuarioLogado());
    }


}
