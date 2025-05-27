package com.generation.blogpessoal.controller;

import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.model.UsuarioLogin;
import com.generation.blogpessoal.repository.UsuarioRepository;
import com.generation.blogpessoal.service.UsuarioService;
import com.generation.blogpessoal.util.TestBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UsuarioControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    private static final String USUARIO_ROOT_EMAIL = "root@email.com";
    private static final String USUARIO_ROOT_SENHA = "rootroot";
    private static final String BASE_URL_USUARIO = "/usuarios";

    @BeforeAll
    void start() {
        usuarioRepository.deleteAll();
        usuarioService.cadastrarUsuario(TestBuilder.criarUsuariRoot());
    }

    @Test
    @DisplayName("01 - Deve Cadastrar um novo usuário com sucesso!")
    public void DeveCadastrarNovoUsuario() {
        // Given
        Usuario usuario = TestBuilder.criarUsuario(null,"Renata","renata@gmail.com.br", "renata123" );

        // When
        HttpEntity<Usuario> requisicao = new HttpEntity<>(usuario);
        ResponseEntity<Usuario> resposta = testRestTemplate.exchange(
                BASE_URL_USUARIO + "/cadastrar", HttpMethod.POST, requisicao, Usuario.class
        );

        //Then
        Assertions.assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        Assertions.assertEquals("Renata", resposta.getBody().getNome());
        Assertions.assertEquals("renata@gmail.com.br", resposta.getBody().getUsuario());
    }

    @Test
    @DisplayName("02 -  Não Deve Permitir a duplicação do Usuário")
    public void NaoDevePermitirDuplicacaoUsuario() {

        Usuario usuario = TestBuilder.criarUsuario(null,"Angelo dos Santos","angelo@gmail.com.br", "angelo123");
        usuarioService.cadastrarUsuario(usuario);

        HttpEntity<Usuario> requisicao = new HttpEntity<>(usuario);
        ResponseEntity<Usuario> resposta = testRestTemplate.exchange(
                BASE_URL_USUARIO + "/cadastrar", HttpMethod.POST, requisicao, Usuario.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());

    }

    @Test
    @DisplayName("03 - Deve atualizar os dados de um usuário com sucesso!")
    public void DeveAtualizarUmUsuario() {
        // Given
        Usuario usuario = TestBuilder.criarUsuario(null,"Geovanna","geovanna@gmail.com.br", "geovanna123" );
        Optional<Usuario> usuarioCadastrado= usuarioService.cadastrarUsuario(usuario);
        var usuarioUpdate = TestBuilder.criarUsuario(usuarioCadastrado.get().getId(), "Giovanna Lucia","geovanna@gmail.com", "geovanna123");

        // When
        HttpEntity<Usuario> requisicao = new HttpEntity<>(usuarioUpdate);
        ResponseEntity<Usuario> resposta = testRestTemplate
                .withBasicAuth(USUARIO_ROOT_EMAIL, USUARIO_ROOT_SENHA)
                .exchange(BASE_URL_USUARIO + "/atualizar", HttpMethod.PUT, requisicao, Usuario.class);

        //Then
        Assertions.assertEquals(HttpStatus.OK, resposta.getStatusCode());
        Assertions.assertEquals("Giovanna Lucia", resposta.getBody().getNome());
        Assertions.assertEquals("geovanna@gmail.com", resposta.getBody().getUsuario());
    }

    @Test
    @DisplayName("04 - Deve listar todos os usuário com sucesso!")
    public void DeveListarTodosUsuario() {
        // Given
        usuarioService.cadastrarUsuario(TestBuilder.criarUsuario(null,"Tatiana","tatiana@gmail.com.br", "tatiana123" ));
        usuarioService.cadastrarUsuario(TestBuilder.criarUsuario(null,"Geovanna","geovanna@gmail.com.br", "geovanna123" ));

        // When
        ResponseEntity<Usuario[]> resposta = testRestTemplate
                .withBasicAuth(USUARIO_ROOT_EMAIL, USUARIO_ROOT_SENHA)
                .exchange(BASE_URL_USUARIO , HttpMethod.GET, null, Usuario[].class);

        //Then
        Assertions.assertEquals(HttpStatus.OK, resposta.getStatusCode());
        Assertions.assertNotNull(resposta.getBody());
    }


    @Test
    @DisplayName("05 - Deve Buscar um usuário por Id com sucesso!")
    public void DeveBuscarUsuarioPorId() {
        // Given
        usuarioService.cadastrarUsuario(TestBuilder.criarUsuario(null,"Tatiana","tatiana@gmail.com.br", "tatiana123" ));
        usuarioService.cadastrarUsuario(TestBuilder.criarUsuario(null,"Geovanna","geovanna@gmail.com.br", "geovanna123" ));

        // When
        ResponseEntity<Usuario> resposta = testRestTemplate
                .withBasicAuth(USUARIO_ROOT_EMAIL, USUARIO_ROOT_SENHA)
                .exchange(BASE_URL_USUARIO , HttpMethod.GET, null, Usuario.class);

        //Then
        Assertions.assertEquals(HttpStatus.OK, resposta.getStatusCode());
        Assertions.assertNotNull(resposta.getBody());
    }

    @Test
    @DisplayName("06 - Deve listar um usuário específico - pelo id")
    public void deveListarUmUsuarioPorId() {

        //Given
        Optional<Usuario> usuario = usuarioService.cadastrarUsuario(TestBuilder.criarUsuario(null, "Ana Luine", "ana_luine@email.com", "analuine123"));
        var id = usuario.get().getId();

        //When
        ResponseEntity<Usuario> resposta = testRestTemplate
                .withBasicAuth(USUARIO_ROOT_EMAIL, USUARIO_ROOT_SENHA)
                .exchange(BASE_URL_USUARIO + "/" + id, HttpMethod.GET, null, Usuario.class);

        //Then
        Assertions.assertEquals(HttpStatus.OK, resposta.getStatusCode());
        Assertions.assertNotNull(resposta.getBody());
    }

    @Test
    @DisplayName("07 - Deve Autenticar um usuário com sucesso")
    public void deveAutenticarUsuario() {

        //Given
        usuarioService.cadastrarUsuario(TestBuilder.criarUsuario(null, "Márcia Marques", "marcia_marques@email.com.br", "13465278"));
        UsuarioLogin usuarioLogin = TestBuilder.criarUsuarioLogin("marcia_marques@email.com.br", "13465278");

        //When
        HttpEntity<UsuarioLogin> requisicao = new HttpEntity<>(usuarioLogin);

        ResponseEntity<UsuarioLogin> resposta = testRestTemplate.exchange(
                BASE_URL_USUARIO + "/logar", HttpMethod.POST, requisicao, UsuarioLogin.class);

        //Then
        Assertions.assertEquals(HttpStatus.OK, resposta.getStatusCode());
        Assertions.assertEquals("marcia_marques@email.com.br", resposta.getBody().getUsuario());
    }
}
