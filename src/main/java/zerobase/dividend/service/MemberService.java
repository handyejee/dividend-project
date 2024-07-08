package zerobase.dividend.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import zerobase.dividend.exception.impl.AlreadyExistUserException;
import zerobase.dividend.model.Auth;
import zerobase.dividend.model.MemberEntity;
import zerobase.dividend.persist.MemberRepository;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.memberRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("couldn't find user -> " + username));
    }

    public MemberEntity register(Auth.SignUp member) {
        boolean exists = this.memberRepository.existsByUserName(member.getUsername());
        if(exists) {
            throw new AlreadyExistUserException();
        }

        member.setPassword(this.passwordEncoder.encode(member.getPassword())); // encoded password

        return this.memberRepository.save(member.toEntity());
    }

    public MemberEntity authenticate(Auth.SignIn member) {
        var user = this.memberRepository.findByUserName(member.getUsername())
                .orElseThrow(() -> new RuntimeException("ID does not exist"));

        if (!this.passwordEncoder.matches(member.getPassword(), user.getPassword())){
            throw new RuntimeException("Password does not match");
        }

        return user;
    }
}
