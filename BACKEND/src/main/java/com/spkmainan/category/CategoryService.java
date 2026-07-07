package com.spkmainan.category;

import com.spkmainan.category.CategoryDto.Request;
import com.spkmainan.category.CategoryDto.Response;
import com.spkmainan.common.exception.ConflictException;
import com.spkmainan.common.exception.ResourceNotFoundException;
import com.spkmainan.toy.ToyRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository repository;
    private final ToyRepository toyRepository;

    public CategoryService(CategoryRepository repository, ToyRepository toyRepository) {
        this.repository = repository;
        this.toyRepository = toyRepository;
    }

    @Transactional(readOnly = true)
    public List<Response> findAll() {
        return repository.findAll().stream()
            .map(c -> Response.from(c, toyRepository.countByCategoryCode(c.getCode())))
            .toList();
    }

    @Transactional(readOnly = true)
    public Response findById(Long id) {
        CategoryEntity c = getOrThrow(id);
        return Response.from(c, toyRepository.countByCategoryCode(c.getCode()));
    }

    @Transactional
    public Response create(Request request) {
        if (repository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Kategori sudah ada: " + request.name());
        }
        CategoryEntity c = new CategoryEntity(uniqueCode(request.name()), request.name(), request.description());
        return Response.from(repository.save(c), 0);
    }

    @Transactional
    public Response update(Long id, Request request) {
        CategoryEntity c = getOrThrow(id);
        c.setName(request.name());
        c.setDescription(request.description());
        return Response.from(repository.save(c), toyRepository.countByCategoryCode(c.getCode()));
    }

    @Transactional
    public void delete(Long id) {
        CategoryEntity c = getOrThrow(id);
        long count = toyRepository.countByCategoryCode(c.getCode());
        if (count > 0) {
            throw new ConflictException(count + " mainan masih memakai kategori ini — pindahkan dulu.");
        }
        repository.delete(c);
    }

    private CategoryEntity getOrThrow(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Kategori tidak ditemukan: " + id));
    }

    /** Slug from the name; append a suffix if taken. */
    private String uniqueCode(String name) {
        String base = name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (base.isBlank()) {
            base = "kategori";
        }
        String code = base;
        int n = 2;
        while (repository.existsByCode(code)) {
            code = base + "-" + n++;
        }
        return code;
    }
}
