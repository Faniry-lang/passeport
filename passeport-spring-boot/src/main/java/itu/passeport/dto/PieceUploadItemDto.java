package itu.passeport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PieceUploadItemDto {
    private Integer pieceId;
    private String pieceLabel;
    private boolean fichierExistant;
    private boolean uploadable;
    private Integer pieceDemandeId;
}
