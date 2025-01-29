package upc.edu.gessi.repo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponseDTO<T> {
    private List<T> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private boolean last;

    public static <T> PageResponseDTO<T> of(List<T> content, int currentPage, int pageSize, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean last = currentPage >= (totalPages - 1);

        return PageResponseDTO.<T>builder()
                .content(content)
                .currentPage(currentPage)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(last)
                .build();
    }
}
