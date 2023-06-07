package seventeam.tgbot.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import seventeam.tgbot.dto.DogDto;
import seventeam.tgbot.service.impl.DogServiceImpl;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DogControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private DogServiceImpl dogService;
    DogDto dogDto = new DogDto("Name", "breed", LocalDate.of(2000, 12, 31), "suit", "gender");

    @Test
    @DisplayName("Проверка создания питомца")
    void createDog() throws Exception {
        mockMvc.perform(post("/dog/new")
                        .param("name", "Name")
                        .param("breed", "breed")
                        .param("dateOfBirth", String.valueOf(LocalDate.of(2000, 12, 31)))
                        .param("suit", "suit")
                        .param("gender", "gender"))
                .andExpect(status().isOk());
        verify(dogService).createDog("Name", "breed", LocalDate.of(2000, 12, 31), "suit", "gender");
    }

    @Test
    @DisplayName("Проверка получения питомца по id")
    void getDog() throws Exception {
        when(dogService.getPet(0L)).thenReturn(dogDto);
        mockMvc.perform(get("/dog/get").param("id", "0"))
                .andExpect(status().isOk());
        verify(dogService).getPet(0L);
    }

    @Test
    @DisplayName("Проверка получения всех питомцев")
    void getAll() throws Exception {
        when(dogService.getAllPets()).thenReturn(List.of(dogDto));
        mockMvc.perform(get("/dog/all"))
                .andExpect(status().isOk());
        verify(dogService).getAllPets();
    }

    @Test
    @DisplayName("Проверка удаления питомца по id")
    void deleteDog() throws Exception {
        mockMvc.perform(delete("/dog/del").param("id", "0"))
                .andExpect(status().isOk());
        verify(dogService).deletePet(0L);
    }
}