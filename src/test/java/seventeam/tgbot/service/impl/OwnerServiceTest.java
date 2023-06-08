package seventeam.tgbot.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seventeam.tgbot.dto.OwnerCatDto;
import seventeam.tgbot.dto.OwnerDogDto;
import seventeam.tgbot.model.Cat;
import seventeam.tgbot.model.CatOwner;
import seventeam.tgbot.model.Dog;
import seventeam.tgbot.model.DogOwner;
import seventeam.tgbot.repository.CatOwnerRepository;
import seventeam.tgbot.repository.DogOwnerRepository;
import seventeam.tgbot.utils.MappingUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnerServiceTest {
    @Mock
    private DogOwnerRepository dogOwnerRepository;
    @Mock
    private CatOwnerRepository catOwnerRepository;
    @Mock
    private DogServiceImpl dogService;
    @Mock
    private CatServiceImpl catService;
    @InjectMocks
    private OwnerService ownerService;
    @Mock
    private MappingUtils mappingUtils;
    @Mock
    Dog dog = new Dog("Name", "breed", LocalDate.of(2000, 12, 31), "suit", "gender");
    @Mock
    Cat cat = new Cat("Name", "breed", LocalDate.of(2000, 12, 31), "suit", "gender");
    @Mock
    DogOwner dogOwner = new DogOwner(0L, 0L, "firstName", "lastName", "7_xxx_xxx_xx_xx", List.of(dog), LocalDateTime.now());
    @Mock
    OwnerDogDto ownerDogDto = new OwnerDogDto(0L, 0L, "firstName", "lastName", "7_xxx_xxx_xx_xx", List.of(dog),
            LocalDateTime.now());
    @Mock
    OwnerCatDto ownerCatDto = new OwnerCatDto(0L, 0L, "firstName", "lastName", "7_xxx_xxx_xx_xx", List.of(cat),
            LocalDateTime.now());
    @Mock
    CatOwner catOwner = new CatOwner(0L, 0L, "firstName", "lastName", "7_xxx_xxx_xx_xx", List.of(cat), LocalDateTime.now());

    @Test
    @DisplayName("Проверка создания владельца")
    void createOwner() {

    }

    @Test
    @DisplayName("Проверка получения владельца")
    void getOwner() {
        when(dogOwnerRepository.getReferenceById(0L)).thenReturn(dogOwner);
        when(mappingUtils.mapToDogOwnerDto(dogOwner)).thenReturn(ownerDogDto);
        assertEquals(ownerDogDto, ownerService.getDogOwner(0L));
        when(catOwnerRepository.getReferenceById(0L)).thenReturn(catOwner);
        when(mappingUtils.mapToCatOwnerDto(catOwner)).thenReturn(ownerCatDto);
        assertEquals(ownerCatDto, ownerService.getCatOwner(0L));
    }

    @Test
    @DisplayName("Проверка обновления владельца")
    void updateOwner() {
        when(dogOwnerRepository.getReferenceById(0L)).thenReturn(dogOwner);
        verify(dogOwnerRepository, verificationData -> ownerService.updateDogOwner(dogOwner.getId(), dogOwner.getChatId(),
                dogOwner.getFirstName(), dogOwner.getLastName(), dogOwner.getPhoneNumber(),
                dogOwner.getProbation())).saveAndFlush(dogOwner);
        when(catOwnerRepository.getReferenceById(0L)).thenReturn(catOwner);
        verify(catOwnerRepository, verificationData -> ownerService.updateCatOwner(catOwner.getId(),
                catOwner.getChatId(),
                catOwner.getFirstName(), catOwner.getLastName(), catOwner.getPhoneNumber(),
                catOwner.getProbation())).saveAndFlush(catOwner);
    }

    @Test
    @DisplayName("Проверка удаления владельца")
    void deleteOwner() {
        verify(dogOwnerRepository, verificationData -> ownerService.deleteDogOwner(0L)).deleteById(0L);
        verify(catOwnerRepository, verificationData -> ownerService.deleteDogOwner(0L)).deleteById(0L);
    }
}