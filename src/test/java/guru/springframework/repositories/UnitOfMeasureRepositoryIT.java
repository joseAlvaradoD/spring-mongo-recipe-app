package guru.springframework.repositories;

import guru.springframework.bootstrap.RecipeBootstrap;
import guru.springframework.domain.UnitOfMeasure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by jt on 6/17/17.
 */
@ExtendWith(SpringExtension.class)
@DataMongoTest
public class UnitOfMeasureRepositoryIT {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    RecipeRepository recipeRepository;

    @Autowired
    UnitOfMeasureRepository unitOfMeasureRepository;

    @BeforeEach
    public void setUp() throws Exception {
        categoryRepository.deleteAll();
        recipeRepository.deleteAll();
        unitOfMeasureRepository.deleteAll();

        RecipeBootstrap bootstrap = new RecipeBootstrap(categoryRepository, recipeRepository, unitOfMeasureRepository);
        bootstrap.onApplicationEvent(null);
    }

    @Test
    public void findByDescription() throws Exception {

        Optional<UnitOfMeasure> uomOptional = unitOfMeasureRepository.findByDescription("Teaspoon");

        assertEquals("Teaspoon", uomOptional.get().getDescription());
    }

    @Test
    public void findByDescriptionCup() throws Exception {

        Optional<UnitOfMeasure> uomOptional = unitOfMeasureRepository.findByDescription("Cup");

        assertEquals("Cup", uomOptional.get().getDescription());
    }

}