package com.martini.fish_gen.service;

import com.github.javafaker.Faker;
import com.martini.fish_gen.model.dao.ApplicationUser;
import com.martini.fish_gen.model.dao.FlashcardCollection;
import com.martini.fish_gen.model.dto.CollectionFilter;
import com.martini.fish_gen.model.dto.CreateCollectionDto;
import com.martini.fish_gen.repository.ApplicationUserRepository;
import com.martini.fish_gen.repository.FlashcardCollectionRepository;
import com.martini.fish_gen.util.LikePatternUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class CollectionService {

    private final FlashcardCollectionRepository flashcardCollectionRepository;
    private final CurrentUserService currentUserService;
    private final ApplicationUserRepository applicationUserRepository;
    private final Faker faker = new Faker();

    @Transactional(readOnly = true)
    public Page<FlashcardCollection> getCollections(CollectionFilter filter, int page, int size) {
        return flashcardCollectionRepository.search(
                currentUserService.getUsername(),
                LikePatternUtils.containsIgnoreCase(filter.name()),
                PageRequest.of(page, size, filter.sortOrDefault().toSort()));
    }

    @Transactional
    public void removeById(Long id) {
        flashcardCollectionRepository.delete(findOwnedCollection(id));
    }

    @Transactional
    public void updateCollectionName(Long id, String newCollectionName) {
        FlashcardCollection flashcardCollection = findOwnedCollection(id);
        flashcardCollection.setName(newCollectionName);
        flashcardCollectionRepository.save(flashcardCollection);
    }

    private FlashcardCollection findOwnedCollection(Long id) {
        return flashcardCollectionRepository.findByIdIsAndUser_Username(id, currentUserService.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Collection with id " + id + " not found"));
    }

    @Transactional
    public void saveCollection(CreateCollectionDto createCollectionDto) {
        String username = currentUserService.getUsername();
        ApplicationUser applicationUser = applicationUserRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Username " + username + " not found"));
        String name = createCollectionDto.name() == null || createCollectionDto.name().isBlank()
                ? faker.color().name() + "_" + faker.animal().name()
                : createCollectionDto.name().trim();
        flashcardCollectionRepository.save(
                FlashcardCollection.builder()
                        .name(name)
                        .user(applicationUser)
                        .build()
        );
    }
}
