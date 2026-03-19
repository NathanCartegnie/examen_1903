package member_service.controller;

import member_service.dto.MemberRequest;
import member_service.entity.Member;
import member_service.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "Gestion des membres de la plateforme")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @Operation(summary = "Inscrire un nouveau membre")
    public ResponseEntity<Member> createMember(@Valid @RequestBody MemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.createMember(request));
    }

    @GetMapping
    @Operation(summary = "Lister tous les membres")
    public ResponseEntity<List<Member>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un membre par ID")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un membre")
    public ResponseEntity<Member> updateMember(@PathVariable Long id,
                                               @Valid @RequestBody MemberRequest request) {
        return ResponseEntity.ok(memberService.updateMember(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un membre (supprime ses réservations via Kafka)")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint interne appelé par le Reservation Service pour vérifier la suspension.
     */
    @GetMapping("/{id}/suspended")
    @Operation(summary = "Vérifier si un membre est suspendu")
    public ResponseEntity<Boolean> isMemberSuspended(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.isMemberSuspended(id));
    }
}
