package dev.codebusters.code_busters.service;

import dev.codebusters.code_busters.domain.AppUser;
import dev.codebusters.code_busters.domain.Country;
import dev.codebusters.code_busters.domain.Submission;
import dev.codebusters.code_busters.domain.UserType;
import dev.codebusters.code_busters.model.AppUserDTO;
import dev.codebusters.code_busters.repos.AppUserRepository;
import dev.codebusters.code_busters.repos.CountryRepository;
import dev.codebusters.code_busters.repos.SubmissionRepository;
import dev.codebusters.code_busters.repos.UserTypeRepository;
import dev.codebusters.code_busters.util.NotFoundException;
import dev.codebusters.code_busters.util.ReferencedWarning;
import dev.codebusters.code_busters.util.ResourceAlreadyExistsException;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final CountryRepository countryRepository;
    private final UserTypeRepository userTypeRepository;
    private final SubmissionRepository submissionRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(final AppUserRepository appUserRepository,
                          final CountryRepository countryRepository, final UserTypeRepository userTypeRepository,
                          final SubmissionRepository submissionRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.countryRepository = countryRepository;
        this.userTypeRepository = userTypeRepository;
        this.submissionRepository = submissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AppUserDTO> findAll() {
        final List<AppUser> appUsers = appUserRepository.findAll(Sort.by("id"));
        return appUsers.stream()
                .map(appUser -> mapToDTO(appUser, new AppUserDTO()))
                .toList();
    }

    public AppUserDTO get(final Long id) {
        return appUserRepository.findById(id)
                .map(appUser -> mapToDTO(appUser, new AppUserDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final AppUserDTO appUserDTO) {
        String email = appUserDTO.getEmail();
        if (appUserRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Email " + email + " is already registered");
        }
        if(appUserDTO.getUserType() == null) appUserDTO.setUserType(1L);

        final AppUser appUser = new AppUser();
        mapToEntity(appUserDTO, appUser);
        appUser.setPassword(passwordEncoder.encode(appUserDTO.getPassword()));
        appUser.setEnabled(false);
        return appUserRepository.save(appUser).getId();
    }

    public void update(final Long id, final AppUserDTO appUserDTO) {
        final AppUser appUser = appUserRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(appUserDTO, appUser);
        appUserRepository.save(appUser);
    }

    public void delete(final Long id) {
        appUserRepository.deleteById(id);
    }



    private AppUserDTO mapToDTO(final AppUser appUser, final AppUserDTO appUserDTO) {
        appUserDTO.setId(appUser.getId());
        appUserDTO.setEmail(appUser.getEmail());
        appUserDTO.setName(appUser.getName());
        appUserDTO.setAdded(appUser.getAdded());
        appUserDTO.setLastActive(appUser.getLastActive());
        appUserDTO.setPassword(appUser.getPassword());
        appUserDTO.setEnabled(appUser.getEnabled());
        appUserDTO.setCountry(appUser.getCountry() == null ? null : appUser.getCountry().getId());
        appUserDTO.setUserType(appUser.getUserType() == null ? null : appUser.getUserType().getId());
        return appUserDTO;
    }

    private AppUser mapToEntity(final AppUserDTO appUserDTO, final AppUser appUser) {
        appUser.setEmail(appUserDTO.getEmail());
        appUser.setName(appUserDTO.getName());
        appUser.setAdded(appUserDTO.getAdded());
        appUser.setLastActive(appUserDTO.getLastActive());
        appUser.setPassword(appUserDTO.getPassword());
        appUser.setEnabled(appUserDTO.getEnabled());
        final Country country = appUserDTO.getCountry() == null ? null : countryRepository.findById(appUserDTO.getCountry())
                .orElseThrow(() -> new NotFoundException("country not found"));
        appUser.setCountry(country);
        final UserType userType = appUserDTO.getUserType() == null ? null : userTypeRepository.findById(appUserDTO.getUserType())
                .orElseThrow(() -> new NotFoundException("userType not found"));
        appUser.setUserType(userType);
        return appUser;
    }

    public ReferencedWarning getReferencedWarning(final Long id) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final AppUser appUser = appUserRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        final Submission userSubmission = submissionRepository.findFirstByUser(appUser);
        if (userSubmission != null) {
            referencedWarning.setKey("appUser.submission.user.referenced");
            referencedWarning.addParam(userSubmission.getId());
            return referencedWarning;
        }
        return null;
    }

}
