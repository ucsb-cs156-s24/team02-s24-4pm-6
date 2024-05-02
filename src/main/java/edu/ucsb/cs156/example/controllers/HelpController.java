package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Help;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.HelpRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.time.LocalDateTime;

@Tag(name = "Help")
@RequestMapping("/api/help")
@RestController
@Slf4j
public class HelpController extends ApiController {

    @Autowired
    HelpRepository helpRepository;

    @Operation(summary= "List help requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Help> allHelp() {
        Iterable<Help> requests = helpRepository.findAll();
        return requests;
    }

    @Operation(summary= "Create a new help request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Help postHelp(
            @Parameter(name="requesterEmail") @RequestParam String requesterEmail,
            @Parameter(name="teamId") @RequestParam String teamId,
            @Parameter(name="tableOrBreakoutRoom") @RequestParam String tableOrBreakoutRoom,
            @Parameter(name="requestTime") @RequestParam("requestTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime requestTime,
            @Parameter(name="explanation") @RequestParam String explanation,
            @Parameter(name="solved") @RequestParam boolean solved)
            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters

        log.info("requestTime={}", requestTime);

        Help help = new Help();
        help.setRequesterEmail(requesterEmail);
        help.setTeamId(teamId);
        help.setTableOrBreakoutRoom(tableOrBreakoutRoom);
        help.setRequestTime(requestTime);
        help.setExplanation(explanation);
        help.setSolved(solved);



        Help savedHelp = helpRepository.save(help);

        return savedHelp;
    }

    @Operation(summary= "Get a single help request")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Help getById(
            @Parameter(name="id") @RequestParam Long id) {
        Help help = helpRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Help.class, id));

        return help;
    }

    @Operation(summary= "Delete a help request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteHelp(
            @Parameter(name="id") @RequestParam Long id) {
        Help help = helpRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Help.class, id));

        helpRepository.delete(help);
        return genericMessage("Help request with id %s deleted".formatted(id));
    }

    @Operation(summary= "Update a single help request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Help updateHelp(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid Help incoming) {

        Help help = helpRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Help.class, id));

        help.setRequesterEmail(incoming.getRequesterEmail());
        help.setTeamId(incoming.getTeamId());
        help.setTableOrBreakoutRoom(incoming.getTableOrBreakoutRoom());
        help.setRequestTime(incoming.getRequestTime());
        help.setExplanation(incoming.getExplanation());
        help.setSolved(incoming.getSolved());

        helpRepository.save(help);

        return help;
    }
}
