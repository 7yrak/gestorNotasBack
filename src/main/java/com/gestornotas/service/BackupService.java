package com.gestornotas.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gestornotas.model.entity.Notebook;
import com.gestornotas.model.entity.Page;
import com.gestornotas.model.entity.PageContentBlock;
import com.gestornotas.model.entity.Resource;
import com.gestornotas.model.entity.Section;
import com.gestornotas.model.entity.SectionGroup;
import com.gestornotas.model.entity.Tag;
import com.gestornotas.model.enums.ContentBlockType;
import com.gestornotas.repository.NotebookRepository;
import com.gestornotas.repository.PageContentBlockRepository;
import com.gestornotas.repository.PageRepository;
import com.gestornotas.repository.ResourceRepository;
import com.gestornotas.repository.SectionGroupRepository;
import com.gestornotas.repository.SectionRepository;
import com.gestornotas.repository.TagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BackupService {
    private final NotebookRepository notebookRepository;
    private final SectionGroupRepository sectionGroupRepository;
    private final SectionRepository sectionRepository;
    private final PageRepository pageRepository;
    private final PageContentBlockRepository contentBlockRepository;
    private final TagRepository tagRepository;
    private final ResourceRepository resourceRepository;

    public BackupService(
            NotebookRepository notebookRepository,
            SectionGroupRepository sectionGroupRepository,
            SectionRepository sectionRepository,
            PageRepository pageRepository,
            PageContentBlockRepository contentBlockRepository,
            TagRepository tagRepository,
            ResourceRepository resourceRepository) {
        this.notebookRepository = notebookRepository;
        this.sectionGroupRepository = sectionGroupRepository;
        this.sectionRepository = sectionRepository;
        this.pageRepository = pageRepository;
        this.contentBlockRepository = contentBlockRepository;
        this.tagRepository = tagRepository;
        this.resourceRepository = resourceRepository;
    }

    @Transactional
    public ImportResult importBackup(UUID userId, JsonNode backup) {
        validateBackup(backup);

        Map<UUID, UUID> notebookIds = new HashMap<>();
        Map<UUID, UUID> groupIds = new HashMap<>();
        Map<UUID, UUID> sectionIds = new HashMap<>();
        Map<UUID, UUID> pageIds = new HashMap<>();
        List<PendingParent<SectionGroup>> groupParents = new ArrayList<>();
        List<PendingParent<Page>> pageParents = new ArrayList<>();
        int notebookCount = 0;
        int sectionCount = 0;
        int pageCount = 0;
        int blockCount = 0;

        for (JsonNode notebookNode : backup.path("notebooks")) {
            UUID oldNotebookId = requiredUuid(notebookNode, "notebookId");
            Notebook notebook = new Notebook();
            notebook.setUserId(userId);
            notebook.setName(requiredText(notebookNode, "name"));
            notebook.setColor(textOr(notebookNode, "color", "#e55b3c"));
            notebook.setIsDeleted(false);
            notebook.setOrderInUser(intOr(notebookNode, "orderInUser", notebookCount));
            notebook = notebookRepository.save(notebook);
            notebookIds.put(oldNotebookId, notebook.getNotebookId());
            notebookCount++;

            for (JsonNode groupNode : notebookNode.path("sectionGroups")) {
                UUID oldGroupId = requiredUuid(groupNode, "sectionGroupId");
                SectionGroup group = new SectionGroup();
                group.setNotebookId(notebook.getNotebookId());
                group.setName(requiredText(groupNode, "name"));
                group.setIsDeleted(false);
                group.setOrderInParent(intOr(groupNode, "orderInParent", 0));
                group = sectionGroupRepository.save(group);
                groupIds.put(oldGroupId, group.getSectionGroupId());
                groupParents.add(new PendingParent<>(group, optionalUuid(groupNode, "parentSectionGroupId")));
            }

            for (JsonNode sectionNode : notebookNode.path("sections")) {
                UUID oldSectionId = requiredUuid(sectionNode, "sectionId");
                Section section = new Section();
                section.setNotebookId(notebook.getNotebookId());
                UUID oldGroupId = optionalUuid(sectionNode, "sectionGroupId");
                section.setSectionGroupId(oldGroupId == null ? null : groupIds.get(oldGroupId));
                section.setName(requiredText(sectionNode, "name"));
                section.setColor(textOr(sectionNode, "color", "#5d9c91"));
                section.setIsDeleted(false);
                section.setOrderInParent(intOr(sectionNode, "orderInParent", sectionCount));
                section = sectionRepository.save(section);
                sectionIds.put(oldSectionId, section.getSectionId());
                sectionCount++;

                for (JsonNode pageNode : sectionNode.path("pages")) {
                    UUID oldPageId = requiredUuid(pageNode, "pageId");
                    Page page = new Page();
                    page.setSectionId(section.getSectionId());
                    page.setTitle(requiredText(pageNode, "title"));
                    page.setIsDeleted(false);
                    page.setOrderInSection(intOr(pageNode, "orderInSection", pageCount));
                    page.setLastModifiedByUserId(userId);
                    page.setVersion(Math.max(1, intOr(pageNode, "version", 1)));
                    page = pageRepository.save(page);
                    pageIds.put(oldPageId, page.getPageId());
                    pageParents.add(new PendingParent<>(page, optionalUuid(pageNode, "parentPageId")));
                    pageCount++;

                    for (JsonNode blockNode : pageNode.path("contentBlocks")) {
                        PageContentBlock block = new PageContentBlock();
                        block.setPageId(page.getPageId());
                        block.setType(parseBlockType(blockNode.path("type").asText("text")));
                        block.setContentData(requiredText(blockNode, "contentData"));
                        block.setXPosition(nullableInt(blockNode, "xPosition"));
                        block.setYPosition(nullableInt(blockNode, "yPosition"));
                        block.setWidth(nullableInt(blockNode, "width"));
                        block.setHeight(nullableInt(blockNode, "height"));
                        block.setZIndex(intOr(blockNode, "zIndex", 0));
                        block.setOrderOnPage(intOr(blockNode, "orderOnPage", blockCount));
                        block.setLastModifiedByUserId(userId);
                        contentBlockRepository.save(block);
                        blockCount++;
                    }
                }
            }
        }

        groupParents.forEach(pending -> {
            if (pending.oldParentId() != null) {
                pending.entity().setParentSectionGroupId(requiredMapped(groupIds, pending.oldParentId(), "grupo de secciones"));
                sectionGroupRepository.save(pending.entity());
            }
        });
        pageParents.forEach(pending -> {
            if (pending.oldParentId() != null) {
                pending.entity().setParentPageId(requiredMapped(pageIds, pending.oldParentId(), "página principal"));
                pageRepository.save(pending.entity());
            }
        });

        int tagCount = importTags(userId, backup.path("tags"));
        int resourceCount = importResources(userId, backup.path("resources"));
        return new ImportResult(notebookCount, sectionCount, pageCount, blockCount, tagCount, resourceCount);
    }

    private int importTags(UUID userId, JsonNode nodes) {
        int count = 0;
        for (JsonNode node : nodes) {
            Tag tag = new Tag();
            tag.setUserId(userId);
            tag.setName(requiredText(node, "name"));
            tag.setColor(textOr(node, "color", "#e55b3c"));
            tagRepository.save(tag);
            count++;
        }
        return count;
    }

    private int importResources(UUID userId, JsonNode nodes) {
        int count = 0;
        for (JsonNode node : nodes) {
            Resource resource = new Resource();
            resource.setUserId(userId);
            resource.setFileName(requiredText(node, "fileName"));
            resource.setMimeType(requiredText(node, "mimeType"));
            resource.setFileSize(node.path("fileSize").asLong(0));
            resource.setStoragePath(requiredText(node, "storagePath"));
            resourceRepository.save(resource);
            count++;
        }
        return count;
    }

    private void validateBackup(JsonNode backup) {
        if (backup == null || !"margen-backup".equals(backup.path("format").asText())) {
            badRequest("El archivo no es un respaldo válido de Margen.");
        }
        if (backup.path("version").asInt(-1) != 1) {
            badRequest("La versión del respaldo no es compatible.");
        }
        if (!backup.path("notebooks").isArray()) {
            badRequest("El respaldo no contiene una lista de cuadernos válida.");
        }
    }

    private ContentBlockType parseBlockType(String value) {
        try {
            return ContentBlockType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            badRequest("Tipo de contenido no compatible: " + value);
            return ContentBlockType.text;
        }
    }

    private UUID requiredUuid(JsonNode node, String field) {
        UUID value = optionalUuid(node, field);
        if (value == null) badRequest("Falta el identificador " + field + ".");
        return value;
    }

    private UUID optionalUuid(JsonNode node, String field) {
        String value = node.path(field).asText("").trim();
        if (value.isEmpty()) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            badRequest("El identificador " + field + " no es válido.");
            return null;
        }
    }

    private String requiredText(JsonNode node, String field) {
        String value = node.path(field).asText("").trim();
        if (value.isEmpty()) badRequest("Falta el campo obligatorio " + field + ".");
        return value;
    }

    private String textOr(JsonNode node, String field, String fallback) {
        String value = node.path(field).asText("").trim();
        return value.isEmpty() ? fallback : value;
    }

    private int intOr(JsonNode node, String field, int fallback) {
        return node.hasNonNull(field) && node.path(field).canConvertToInt() ? node.path(field).asInt() : fallback;
    }

    private Integer nullableInt(JsonNode node, String field) {
        return node.hasNonNull(field) && node.path(field).canConvertToInt() ? node.path(field).asInt() : null;
    }

    private UUID requiredMapped(Map<UUID, UUID> ids, UUID oldId, String relation) {
        UUID mapped = ids.get(oldId);
        if (mapped == null) badRequest("El respaldo contiene una relación inválida para " + relation + ".");
        return mapped;
    }

    private void badRequest(String message) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private record PendingParent<T>(T entity, UUID oldParentId) {}

    public record ImportResult(int notebooks, int sections, int pages, int contentBlocks, int tags, int resources) {}
}
