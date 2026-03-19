package member_service.service;

import member_service.dto.MemberRequest;
import member_service.entity.Member;
import member_service.kafka.MemberKafkaProducer;
import member_service.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberKafkaProducer kafkaProducer;

    public Member createMember(MemberRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un membre avec l'email " + request.getEmail() + " existe déjà");
        }
        Member member = new Member();
        member.setFullName(request.getFullName());
        member.setEmail(request.getEmail());
        member.setSubscriptionType(request.getSubscriptionType());
        member.setSuspended(false);
        member.setMaxConcurrentBookings(request.getSubscriptionType().getMaxConcurrentBookings());
        return memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membre introuvable avec l'id : " + id));
    }

    public Member updateMember(Long id, MemberRequest request) {
        Member member = getMemberById(id);
        member.setFullName(request.getFullName());
        member.setEmail(request.getEmail());
        member.setSubscriptionType(request.getSubscriptionType());
        member.setMaxConcurrentBookings(request.getSubscriptionType().getMaxConcurrentBookings());
        return memberRepository.save(member);
    }

    /**
     * Met à jour la suspension d'un membre.
     * Appelé par le consumer Kafka quand une réservation est créée/annulée/complétée.
     */
    public void updateSuspension(Long memberId, boolean suspended) {
        Member member = getMemberById(memberId);
        member.setSuspended(suspended);
        memberRepository.save(member);
        log.info("Membre {} suspension mise à jour : {}", memberId, suspended);
    }

    /**
     * Supprime un membre et publie un événement Kafka pour supprimer
     * toutes ses réservations.
     */
    public void deleteMember(Long id) {
        Member member = getMemberById(id);
        memberRepository.delete(member);
        kafkaProducer.sendMemberDeletedEvent(id);
        log.info("Membre {} supprimé, événement Kafka publié", id);
    }

    /**
     * Endpoint interne appelé par le Reservation Service.
     */
    @Transactional(readOnly = true)
    public boolean isMemberSuspended(Long id) {
        return getMemberById(id).isSuspended();
    }
}
