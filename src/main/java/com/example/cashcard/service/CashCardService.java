package com.example.cashcard.service;

import com.example.cashcard.model.CashCard;
import com.example.cashcard.repository.CashCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;

@Service
public class CashCardService {

    private final CashCardRepository cashCardRepository;
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
        return cashCardRepository.findById(id);
    }

    /**
     * This service create a cashcard, the cashcar id is auto generated, the onwer is set to
     * principal
     * @param cashcard a cashcard object with amount
     * @param owner principal
     * @return a saved cashcard object
     */
    public CashCard createCashCard(CashCard cashcard, String owner){
        CashCard newCashCard = new CashCard(null, cashcard.getAmount(), owner);
        return cashCardRepository.save(newCashCard);
    }

    /**
     *  This service retrieve a list of cashcards by the page setting
     * @param pageable page setting in the url
     * @return a page of cashcards
     */
    public Page<CashCard> findAll(Pageable pageable){
        return cashCardRepository.findAll(pageable);
    }

    /**
     * This service retrieve cashcard by owner and id.
     * @param id cashcard's id
     * @param owner principal
     * @return Optional <cashcard>
     */
    public Optional<CashCard> findByIdAndOwner(Long id, String owner){
        Optional<CashCard> cashCard = cashCardRepository.findByIdAndOwner(id,owner);
        return cashCard;
    }

    /**
     * This service find a list of cash card by owner.
     * @param pageable path parameter from the url
     * @param owner authenticated user
     * @return A page of cashcash that follow the specific page setting from the user.
     */
    public Page<CashCard> findByOwner(Pageable pageable,String owner){
        return cashCardRepository.findByOwner(owner, PageRequest
                .of(pageable.getPageNumber(), pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.DESC,"amount"))));
    }

    /**
     * Update cash card service, this service only update the cash card, it does not
     * create a new cash card.
     * This method looks up whether the updating cash card exists
      * @param id cash card id
     * @param updateCashCard requested http body
     * @param logInAs principal
     * @return false if the cash card does not exist
     *         true if the update is success
     */
    public boolean updateCashCard(Long id, CashCard updateCashCard, String logInAs){
        Optional<CashCard> cashCard = findByIdAndOwner(id, logInAs);
        if (!cashCard.isPresent()){
            return false;
        }
        CashCard cashCardUpdated = new CashCard(cashCard.get().getId(), updateCashCard.getAmount(), logInAs);
        cashCardRepository.save(cashCardUpdated);
        return true;
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
        boolean exist = cashCardRepository.existsByIdAndOwner(id, logInAs);
        if (exist) {
            cashCardRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
