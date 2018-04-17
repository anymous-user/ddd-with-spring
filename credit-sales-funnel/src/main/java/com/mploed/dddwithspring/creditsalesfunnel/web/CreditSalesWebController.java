package com.mploed.dddwithspring.creditsalesfunnel.web;

import com.mploed.dddwithspring.creditsalesfunnel.model.CreditApplicationForm;
import com.mploed.dddwithspring.creditsalesfunnel.model.applicant.Applicant;
import com.mploed.dddwithspring.creditsalesfunnel.model.financing.Financing;
import com.mploed.dddwithspring.creditsalesfunnel.model.household.Household;
import com.mploed.dddwithspring.creditsalesfunnel.model.realEstate.RealEstateProperty;
import com.mploed.dddwithspring.creditsalesfunnel.model.validation.ApplicationSubmissionGroup;
import com.mploed.dddwithspring.creditsalesfunnel.repository.ApplicantRepository;
import com.mploed.dddwithspring.creditsalesfunnel.repository.FinancingRepository;
import com.mploed.dddwithspring.creditsalesfunnel.repository.HouseholdRepository;
import com.mploed.dddwithspring.creditsalesfunnel.repository.RealEstatePropertyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.UUID;

@Controller
public class CreditSalesWebController {
	private final Logger LOGGER = LoggerFactory.getLogger(CreditSalesWebController.class);

	private ApplicantRepository applicantRepository;

	private FinancingRepository financingRepository;

	private HouseholdRepository householdRepository;

	private RealEstatePropertyRepository realEstatePropertyRepository;

	private Validator validator;

	@Autowired
	public CreditSalesWebController(ApplicantRepository applicantRepository, FinancingRepository financingRepository, HouseholdRepository householdRepository, RealEstatePropertyRepository realEstatePropertyRepository, Validator validator) {
		this.applicantRepository = applicantRepository;
		this.financingRepository = financingRepository;
		this.householdRepository = householdRepository;
		this.realEstatePropertyRepository = realEstatePropertyRepository;
		this.validator = validator;
	}



	@GetMapping(path = "/")
	public String index() {
		return "index";
	}

	@PostMapping(path = "/")
	public RedirectView createApplicationNumber() {
		String applicationNumber = UUID.randomUUID().toString();
		return new RedirectView("/application/"+applicationNumber);
	}


	@GetMapping(path = "/application/{applicationNumber}")
	public String applicationOverview(Model model, @PathVariable String applicationNumber) {

		Applicant firstApplicant = applicantRepository.findByApplicationNumberAndApplicantNumber(applicationNumber, "1");
		boolean firstApplicantValid = false;
		if(firstApplicant != null) {
			Set<ConstraintViolation<Applicant>> constraintViolations = validator.validate(firstApplicant, ApplicationSubmissionGroup.class);
			firstApplicantValid = constraintViolations.isEmpty();
		}

		Applicant secondApplicant = applicantRepository.findByApplicationNumberAndApplicantNumber(applicationNumber, "2");
		boolean secondApplicantValid = false;
		if(secondApplicant != null) {
			Set<ConstraintViolation<Applicant>> constraintViolations = validator.validate(secondApplicant, ApplicationSubmissionGroup.class);
			secondApplicantValid = constraintViolations.isEmpty();
		}

		Household household = householdRepository.findByApplicationNumber(applicationNumber);
		boolean householdValid = false;
		if(household != null) {
			Set<ConstraintViolation<Household>> constraintViolations = validator.validate(household, ApplicationSubmissionGroup.class);
			householdValid = constraintViolations.isEmpty();
		}

		Financing financing = financingRepository.findByApplicationNumber(applicationNumber);
		boolean financingValid = false;
		if(financing != null) {
			Set<ConstraintViolation<Financing>> constraintViolations = validator.validate(financing, ApplicationSubmissionGroup.class);
			financingValid = constraintViolations.isEmpty();
		}

		RealEstateProperty realEstateProperty = realEstatePropertyRepository.findByApplicationNumber(applicationNumber);
		boolean realEstatePropertyValid = false;
		if(realEstateProperty != null) {
			Set<ConstraintViolation<RealEstateProperty>> constraintViolations = validator.validate(realEstateProperty, ApplicationSubmissionGroup.class);
			realEstatePropertyValid = constraintViolations.isEmpty();
		}

		model.addAttribute("firstApplicant", firstApplicant);
		model.addAttribute("secondApplicant", secondApplicant);
		model.addAttribute("financing", financing);
		model.addAttribute("household", household);
		model.addAttribute("realEstateProperty", realEstateProperty);
		model.addAttribute("firstApplicantValid", firstApplicantValid );
		model.addAttribute("secondApplicantValid", secondApplicantValid );
		model.addAttribute("financingValid", financingValid );
		model.addAttribute("householdValid", householdValid );
		model.addAttribute("realEstatePropertyValid", realEstatePropertyValid );

		model.addAttribute("applicationNumber", applicationNumber);
		return "applicationOverview";
	}

	@GetMapping(path = "/application/{applicationNumber}/applicant/{applicantNumber}")
	public String applicant(Model model, @PathVariable String applicationNumber, @PathVariable String applicantNumber) {
		Applicant applicant = applicantRepository.findByApplicationNumberAndApplicantNumber(applicationNumber, applicantNumber);
		if (applicant == null) {
			applicant = new Applicant(applicationNumber, applicantNumber);
		}
		model.addAttribute("applicant", applicant);
		return "applicant";
	}

	@PostMapping(path = "/application/{applicationNumber}/applicant/{applicantNumber}")
	public RedirectView saveApplicant(@ModelAttribute Applicant applicant, @PathVariable String applicationNumber, @PathVariable String applicantNumber) {
		applicantRepository.save(applicant);
		return new RedirectView("/application/" + applicationNumber);
	}

	@GetMapping(path = "/application/{applicationNumber}/household")
	public String household(Model model, @PathVariable String applicationNumber) {
		Household household = householdRepository.findByApplicationNumber(applicationNumber);
		if(household == null) {
			household = new Household(applicationNumber);
		}
		model.addAttribute("household", household);
		return "household";
	}

	@PostMapping(path = "/application/{applicationNumber}/household")
	public RedirectView saveHousehold(@ModelAttribute Household household, @PathVariable String applicationNumber) {
		householdRepository.save(household);
		return new RedirectView("/application/" + applicationNumber);
	}

	@GetMapping(path = "/application/{applicationNumber}/realEstateProperty")
	public String realEstateProperty(Model model, @PathVariable String applicationNumber) {
		RealEstateProperty realEstateProperty = realEstatePropertyRepository.findByApplicationNumber(applicationNumber);
		if(realEstateProperty == null) {
			realEstateProperty = new RealEstateProperty(applicationNumber);
		}
		model.addAttribute("realEstateProperty", realEstateProperty);
		return "realEstateProperty";
	}

	@PostMapping(path = "/application/{applicationNumber}/realEstateProperty")
	public RedirectView saveRealEstateProperty(@ModelAttribute RealEstateProperty realEstateProperty, @PathVariable String applicationNumber) {
		realEstatePropertyRepository.save(realEstateProperty);
		return new RedirectView("/application/" + applicationNumber);
	}

	@GetMapping(path = "/application/{applicationNumber}/financing")
	public String financing(Model model, @PathVariable String applicationNumber) {
		Financing financing = financingRepository.findByApplicationNumber(applicationNumber);
		if(financing == null) {
			financing = new Financing(applicationNumber);
		}
		model.addAttribute("financing", financing);
		return "financing";
	}

	@PostMapping(path = "/application/{applicationNumber}/financing")
	public RedirectView saveFinancing(@ModelAttribute Financing financing, @PathVariable String applicationNumber) {
		financingRepository.save(financing);
		return new RedirectView("/application/" + applicationNumber);
	}

}