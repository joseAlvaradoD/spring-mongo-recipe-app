package guru.springframework.controllers;

import guru.springframework.commands.IngredientCommand;
import guru.springframework.commands.RecipeCommand;
import guru.springframework.commands.UnitOfMeasureCommand;
import guru.springframework.services.ImageService;
import guru.springframework.services.IngredientService;
import guru.springframework.services.RecipeService;
import guru.springframework.services.UnitOfMeasureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebFluxTest
@Import(IngredientController.class)
public class IngredientControllerTest {

    @MockBean
    IngredientService ingredientService;

    @MockBean
    UnitOfMeasureService unitOfMeasureService;

    @MockBean
    RecipeService recipeService;

    @MockBean
    ImageService imageService;

    @Autowired
    WebTestClient webTestClient;

    @Test
    public void testListIngredients() throws Exception {
        //given
        RecipeCommand recipeCommand = new RecipeCommand();

        //when
        when(recipeService.findCommandById(anyString())).thenReturn(Mono.just(recipeCommand));

        //then
        List<String> html = webTestClient.get().uri("/recipe/1/ingredients")
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(String.class)
                .getResponseBody()
                .collectList()
                .block();
        verify(recipeService, times(1)).findCommandById(anyString());
        assertNotNull(html);
    }

    @Test
    public void testShowIngredient() throws Exception {
        //given
        IngredientCommand ingredientCommand = new IngredientCommand();
        ingredientCommand.setUom(new UnitOfMeasureCommand());

        //when
        when(ingredientService.findByRecipeIdAndIngredientId(anyString(), anyString())).thenReturn(Mono.just(ingredientCommand));

        //then
        List<String> html = webTestClient.get().uri("/recipe/1/ingredient/2/show")
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody()
                .collectList()
                .block();
        assertNotNull(html);
    }

    @Test
    public void testNewIngredientForm() throws Exception {
        //given
        UnitOfMeasureCommand unitOfMeasureCommand = new UnitOfMeasureCommand();
        unitOfMeasureCommand.setId("1");
        RecipeCommand recipeCommand = new RecipeCommand();
        recipeCommand.setId("1");
        IngredientCommand ingredientCommand = new IngredientCommand();
        ingredientCommand.setUom(unitOfMeasureCommand);
        recipeCommand.getIngredients().add(ingredientCommand);

        //when
        when(recipeService.findCommandById(anyString())).thenReturn(Mono.just(recipeCommand));
        when(unitOfMeasureService.listAllUoms()).thenReturn(Flux.just(unitOfMeasureCommand));

        //then
        List<String> html = webTestClient.get().uri("/recipe/1/ingredient/new")
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody()
                .collectList()
                .block();

        verify(recipeService, times(1)).findCommandById(anyString());
        assertNotNull(html);

    }

    @Test
    public void testUpdateIngredientForm() throws Exception {
        //given
        UnitOfMeasureCommand unitOfMeasureCommand = new UnitOfMeasureCommand();
        unitOfMeasureCommand.setId("1");
        IngredientCommand ingredientCommand = new IngredientCommand();
        ingredientCommand.setUom(unitOfMeasureCommand);

        //when
        when(ingredientService.findByRecipeIdAndIngredientId(anyString(), anyString())).thenReturn(Mono.just(ingredientCommand));
        when(unitOfMeasureService.listAllUoms()).thenReturn(Flux.just(unitOfMeasureCommand));

        //then
        List<String> html = webTestClient.get().uri("/recipe/1/ingredient/2/update")
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody()
                .collectList()
                .block();

        assertNotNull(html);
    }

    @Test
    public void testSaveOrUpdate() throws Exception {
        //given
        IngredientCommand command = new IngredientCommand();
        command.setId("3");
        command.setRecipeId("2");

        //when
        when(ingredientService.saveIngredientCommand(any())).thenReturn(Mono.just(command));

        //then
        List<String> html = webTestClient.post().uri("/recipe/2/ingredient")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("id","")
                    .with("description", "some string"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .returnResult(String.class)
                .getResponseBody()
                .collectList()
                .block();

        assertNotNull(html);

    }

    @Test
    public void testDeleteIngredient() throws Exception {

        //then
        List<String> html = webTestClient.get().uri("/recipe/2/ingredient/3/delete")
                .exchange()
                .expectStatus().is3xxRedirection()
                .returnResult(String.class)
                .getResponseBody()
                .collectList()
                .block();

        verify(ingredientService, times(1)).deleteById(anyString(), anyString());
        assertNotNull(html);

    }
}