package com.example.cashcard.service;

import com.example.cashcard.dto.CashCardBulkUpdateDTO;
import com.example.cashcard.dto.CashCardRequestDTO;
import com.example.cashcard.model.CashCard;
import com.example.cashcard.repository.CashCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CashCardService {

    private final CashCardRepository cashCardRepository;
    private static final Logger log = LoggerFactory.getLogger(CashCardService.class);
    @Autowired
    public CashCardService (CashCardRepository cashCardRepository){
        this.cashCardRepository = cashCardRepository;
    }

    /**
     * This service retrieve a cashcard by id (not used anymore)
     * @param id cashcard id
     * @return a Optional<CashCard>
     */
    public Optional<CashCard> findById(Long id){
        log.info("Service findById starts.");
        log.info("Service findById ends.");
        return cashCardRepository.findById(id);
    }

    /**
     * This service create a cashcard, the cashcar id is auto generated, the onwer is set to
     * principal
     * @param cashCardRequestDTO a cashcardDTO object with only amount
     * @param owner principal
     * @return a saved cashcard object
     */
    public CashCard createCashCard(CashCardRequestDTO cashCardRequestDTO, String owner){
        log.info("Service createCashCard starts.");
        CashCard newCashCard = new CashCard(null, cashCardRequestDTO.getAmount(), owner);
        log.info("Service createCashCard ends.");
        return cashCardRepository.save(newCashCard);
    }

    /**
     *  This service retrieve a list of cashcards by the page setting - not using
     * @param pageable page setting in the url
     * @return a page of cashcards
     */
    public Page<CashCard> findAll(Pageable pageable){
        log.info("Service findAll starts.");
        log.info("Service findAll ends.");
        return cashCardRepository.findAll(pageable);
    }

    /**
     * This service retrieve cashcard by owner and id.
     * @param id cashcard's id
     * @param owner principal
     * @return Optional <cashcard>
     */
    public Optional<CashCard> findByIdAndOwner(Long id, String owner){
        log.info("Service findByIdAndOwner starts.");
        Optional<CashCard> cashCard = cashCardRepository.findByIdAndOwner(id,owner);
        log.info("Service findByIdAndOwner ends.");
        return cashCard;
    }

    /**
     * This service find a list of cash card by owner.
     * @param pageable path parameter from the url
     * @param owner authenticated user
     * @return A page of cashcash that follow the specific page setting from the user.
     */
    public Page<CashCard> findByOwner(Pageable pageable,String owner){
        log.info("Service findByOwner starts.");
        log.info("Service findByOwner ends.");
        return cashCardRepository.findByOwner(owner, PageRequest
                .of(pageable.getPageNumber(), pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.DESC,"amount"))));
    }

    /**
     * Update cash card service, this service only update the cash card, it does not
     * create a new cash card.
     * This method looks up whether the updating cash card exists
      * @param id cash card id
     * @param cashCardRequestDTO requested http body
     * @param logInAs principal
     * @return false if the cash card does not exist
     *         true if the update is success
     */
    public boolean updateCashCard(Long id, CashCardRequestDTO cashCardRequestDTO, String logInAs){
        log.info("Service updateCashCard starts.");
        Optional<CashCard> cashCard = findByIdAndOwner(id, logInAs);
        if (!cashCard.isPresent()){
            log.info("Service updateCashCard ends with cashcard not found.");
            return false;
        }
        CashCard cashCardUpdated = new CashCard(cashCard.get().getId(), cashCardRequestDTO.getAmount(), logInAs);
        cashCardRepository.save(cashCardUpdated);
        log.info("Service updateCashCard ends correctly.");
        return true;
    }


    /**
     * Update all cashcards listed in the request body
     * @param cashCardBulkUpdateDTOS
     * @param owner
     */
    public void bulkUpdate(List<CashCardBulkUpdateDTO> cashCardBulkUpdateDTOS, String owner){
        List<CashCard> cashCards = new ArrayList<>();
        for(CashCardBulkUpdateDTO dto: cashCardBulkUpdateDTOS){
            Optional<CashCard> cashcardOptional = cashCardRepository.findByIdAndOwner(dto.getId(), owner);
            if (cashcardOptional.isPresent()){
                cashCards.add(new CashCard(cashcardOptional.get().getId(), dto.getAmount(), owner));
            }
        }
        cashCardRepository.saveAll(cashCards);
    }

    /**
     * This service first check if the deleting cashcard's existence and ownership,
     * then call deleteById() in JPA to delete.
     * @param id cashcard id
     * @param logInAs principal
     * @return false if the cashcard does not exist or ownership is wrong
     *         true if deleted.
     */
    public boolean deleteCashCard(Long id, String logInAs){
        log.info("Service deleteCashCard starts.");

        boolean exist = cashCardRepository.existsByIdAndOwner(id, logInAs);
        if (exist) {
            cashCardRepository.deleteById(id);
            log.info("Service deletCashCard ends successfully.");

            return true;
        }
        log.info("Service updateCashCard ends with not found.");
        return false;
    }

}
