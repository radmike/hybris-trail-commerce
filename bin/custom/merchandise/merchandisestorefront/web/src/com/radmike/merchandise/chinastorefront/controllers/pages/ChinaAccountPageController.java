/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2013 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 * 
 *  
 */
package com.radmike.merchandise.chinastorefront.controllers.pages;

import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.Breadcrumb;
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.ResourceBreadcrumbBuilder;
import de.hybris.platform.acceleratorstorefrontcommons.forms.validation.AddressValidator;
import de.hybris.platform.acceleratorstorefrontcommons.forms.validation.EmailValidator;
import de.hybris.platform.acceleratorstorefrontcommons.forms.validation.PasswordValidator;
import de.hybris.platform.acceleratorstorefrontcommons.forms.validation.ProfileValidator;
import de.hybris.platform.acceleratorstorefrontcommons.forms.verification.AddressVerificationResultHandler;
import de.hybris.platform.chinaaccelerator.facades.data.CityData;
import de.hybris.platform.chinaaccelerator.facades.data.DistrictData;
import de.hybris.platform.chinaaccelerator.facades.location.CityFacade;
import de.hybris.platform.chinaaccelerator.facades.location.DistrictFacade;
import de.hybris.platform.chinaaccelerator.facades.order.ChinaOrderFacade;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.address.AddressVerificationFacade;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.i18n.I18NFacade;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.OrderFacade;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import com.radmike.merchandise.chinastorefront.controllers.ControllerConstants;
import com.radmike.merchandise.chinastorefront.forms.ChinaAddressForm;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//import de.hybris.platform.ychinaaccelerator.data.CityData;
//import de.hybris.platform.ychinaaccelerator.data.DistrictData;


/**
 * Controller for home page
 */
@Controller
@Scope("tenant")
@RequestMapping("/my-account")
public class ChinaAccountPageController extends AccountPageController
{
	// Internal Redirects
	private static final String REDIRECT_MY_ACCOUNT = REDIRECT_PREFIX + "/my-account";
	private static final String REDIRECT_TO_ADDRESS_BOOK_PAGE = REDIRECT_PREFIX + "/my-account/address-book";

	/**
	 * We use this suffix pattern because of an issue with Spring 3.1 where a Uri value is incorrectly extracted if it
	 * contains on or more '.' characters. Please see https://jira.springsource.org/browse/SPR-6164 for a discussion on
	 * the issue and future resolution.
	 */
	private static final String ORDER_CODE_PATH_VARIABLE_PATTERN = "{orderCode:.*}";
	private static final String ADDRESS_CODE_PATH_VARIABLE_PATTERN = "{addressCode:.*}";

	// CMS Pages
	private static final String ADDRESS_BOOK_CMS_PAGE = "address-book";
	private static final String ADD_EDIT_ADDRESS_CMS_PAGE = "add-edit-address";
	private static final String ORDER_DETAIL_CMS_PAGE = "order";

	private static final Logger LOG = Logger.getLogger(ChinaAccountPageController.class);

	@Resource(name = "orderFacade")
	private OrderFacade orderFacade;

	@Resource(name = "acceleratorCheckoutFacade")
	private CheckoutFacade checkoutFacade;

	@Resource(name = "userFacade")
	protected UserFacade userFacade;

	@Resource(name = "customerFacade")
	protected CustomerFacade customerFacade;

	@Resource(name = "accountBreadcrumbBuilder")
	private ResourceBreadcrumbBuilder accountBreadcrumbBuilder;

	@Resource(name = "passwordValidator")
	private PasswordValidator passwordValidator;

	@Resource(name = "addressValidator")
	private AddressValidator addressValidator;

	@Resource(name = "profileValidator")
	private ProfileValidator profileValidator;

	@Resource(name = "emailValidator")
	private EmailValidator emailValidator;

	@Resource(name = "i18NFacade")
	private I18NFacade i18NFacade;

	@Resource(name = "addressVerificationFacade")
	private AddressVerificationFacade addressVerificationFacade;

	@Resource(name = "addressVerificationResultHandler")
	private AddressVerificationResultHandler addressVerificationResultHandler;

	@Resource(name = "modelService")
	private ModelService modelService;

	@Resource(name = "cityFacade")
	private CityFacade cityFacade;

	@Resource(name = "districtFacade")
	private DistrictFacade districtFacade;

	protected CityFacade getCityFacade()
	{
		return this.cityFacade;
	}

	protected DistrictFacade getDistrictFacade()
	{
		return this.districtFacade;
	}

	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}


	@RequestMapping(value = "/cancelOrder/" + ORDER_CODE_PATH_VARIABLE_PATTERN, method = RequestMethod.GET)
	@RequireHardLogIn
	public String cancelOrder(@PathVariable("orderCode") final String orderCode, final Model model)
			throws CMSItemNotFoundException
	{
		try
		{
			((ChinaOrderFacade) orderFacade).cancelOrder(orderCode);

		}
		catch (final UnknownIdentifierException e)
		{
			LOG.warn("Attempted to load a order that does not exist or is not visible", e);
			return REDIRECT_MY_ACCOUNT;
		}
		storeCmsPageInModel(model, getContentPageForLabelOrId(ORDER_DETAIL_CMS_PAGE));
		model.addAttribute("metaRobots", "no-index,no-follow");
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ORDER_DETAIL_CMS_PAGE));
		return "redirect:/my-account/orders";
	}

	@Override
	@RequestMapping(value = "/address-book", method = RequestMethod.GET)
	@RequireHardLogIn
	public String getAddressBook(final Model model) throws CMSItemNotFoundException
	{
		model.addAttribute("addressData", userFacade.getAddressBook());

		storeCmsPageInModel(model, getContentPageForLabelOrId(ADDRESS_BOOK_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ADDRESS_BOOK_CMS_PAGE));
		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs("text.account.addressBook"));
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountChinaAddressBookPage;
	}

	/**
	 * add-address : adding new address, editing existing address
	 * 
	 * @param model
	 * @return
	 * @throws CMSItemNotFoundException
	 */
	@Override
	@RequestMapping(value = "/add-address", method = RequestMethod.GET)
	@RequireHardLogIn
	public String addAddress(final Model model) throws CMSItemNotFoundException
	{
		model.addAttribute("formActionURL", "add-address");

		model.addAttribute("countryData", checkoutFacade.getDeliveryCountries());
		model.addAttribute("titleData", userFacade.getTitles());
		final ChinaAddressForm addressForm = getPreparedAddressForm();
		model.addAttribute("addressForm", addressForm);
		model.addAttribute("addressBookEmpty", Boolean.valueOf(userFacade.isAddressBookEmpty()));
		model.addAttribute("isDefaultAddress", Boolean.FALSE);
		storeCmsPageInModel(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));

		final List<Breadcrumb> breadcrumbs = accountBreadcrumbBuilder.getBreadcrumbs(null);
		breadcrumbs.add(new Breadcrumb("/my-account/address-book", getMessageSource().getMessage("text.account.addressBook", null,
				getI18nService().getCurrentLocale()), null));
		breadcrumbs.add(new Breadcrumb("#", getMessageSource().getMessage("text.account.addressBook.addEditAddress", null,
				getI18nService().getCurrentLocale()), null));
		model.addAttribute("breadcrumbs", breadcrumbs);
		model.addAttribute("metaRobots", "no-index,no-follow");
		model.addAttribute("regions", getI18NFacade().getRegionsForCountryIso("CN"));
		return ControllerConstants.Views.Pages.Account.AccountChinaEditAddressPage;
	}

	@Override
	protected ChinaAddressForm getPreparedAddressForm()
	{
		final CustomerData currentCustomerData = customerFacade.getCurrentCustomer();
		final ChinaAddressForm addressForm = new ChinaAddressForm();
		addressForm.setFirstName(currentCustomerData.getFirstName());
		addressForm.setLastName(currentCustomerData.getLastName());
		addressForm.setTitleCode(currentCustomerData.getTitleCode());
		return addressForm;
	}


	@RequestMapping(value = "/add-address", method = RequestMethod.POST)
	@RequireHardLogIn
	public String addAddress(final ChinaAddressForm addressForm, final BindingResult bindingResult, final Model model,
			final HttpServletRequest request, final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		//		getAddressValidator().validate(addressForm, bindingResult);
		//		if (bindingResult.hasErrors())
		//		{
		//			GlobalMessages.addErrorMessage(model, "form.global.error");
		//			storeCmsPageInModel(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));
		//			setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));
		//			setUpAddressFormAfterError(addressForm, model);
		//			//			return ControllerConstants.Views.Pages.Account.AccountEditAddressPage;
		//			return ControllerConstants.Views.Pages.Account.AccountChinaEditAddressPage;
		//
		//		}

		final AddressData newAddress = new AddressData();
		newAddress.setFirstName(addressForm.getFirstName());
		newAddress.setLine1(addressForm.getLine1());
		newAddress.setPostalCode(addressForm.getPostcode());
		newAddress.setShippingAddress(true);
		newAddress.setVisibleInAddressBook(true);

		addressForm.setCountryIso("CN");

		newAddress.setCountry(getI18NFacade().getCountryForIsocode(addressForm.getCountryIso()));

		if (addressForm.getRegionIso() != null && !StringUtils.isEmpty(addressForm.getRegionIso()))
		{
			newAddress.setRegion(getI18NFacade().getRegion(addressForm.getCountryIso(), addressForm.getRegionIso()));
		}

		// store city and district if any
		if (addressForm.getCityCode() != null && !StringUtils.isEmpty(addressForm.getCityCode()))
		{
			final CityData cityData = this.getCityFacade().getCityForCode(addressForm.getCityCode());
			newAddress.setCityData(cityData);
		}
		if (addressForm.getCityDistrictCode() != null && !StringUtils.isEmpty(addressForm.getCityDistrictCode()))
		{
			final DistrictData districtData = this.getDistrictFacade().getDistrictByCode(addressForm.getCityDistrictCode());
			newAddress.setCityDistrictData(districtData);
		}

		// store phone
		if (addressForm.getLandlinePhonePart1() != null && !StringUtils.isEmpty(addressForm.getLandlinePhonePart1()))
		{
			newAddress.setLandlinePhonePart1(addressForm.getLandlinePhonePart1());
		}
		if (addressForm.getLandlinePhonePart2() != null && !StringUtils.isEmpty(addressForm.getLandlinePhonePart2()))
		{
			newAddress.setLandlinePhonePart2(addressForm.getLandlinePhonePart2());
		}
		if (addressForm.getLandlinePhonePart3() != null && !StringUtils.isEmpty(addressForm.getLandlinePhonePart3()))
		{
			newAddress.setLandlinePhonePart3(addressForm.getLandlinePhonePart3());
		}

		if (addressForm.getCellPhone() != null && !StringUtils.isEmpty(addressForm.getCellPhone()))
		{
			newAddress.setCellphone(addressForm.getCellPhone());
		}
		// 


		if (userFacade.isAddressBookEmpty())
		{
			newAddress.setDefaultAddress(true);
			newAddress.setVisibleInAddressBook(true);
		}
		else
		{
			newAddress.setDefaultAddress(addressForm.getDefaultAddress() != null && addressForm.getDefaultAddress().booleanValue());
		}

		//		final AddressVerificationResult<AddressVerificationDecision> verificationResult = getAddressVerificationFacade()
		//				.verifyAddressData(newAddress);
		//		final boolean addressRequiresReview = getAddressVerificationResultHandler().handleResult(verificationResult, newAddress,
		//				model, redirectModel, bindingResult, getAddressVerificationFacade().isCustomerAllowedToIgnoreAddressSuggestions(),
		//				"checkout.multi.address.added");
		//
		//		if (addressRequiresReview)
		//		{
		//			model.addAttribute("regions", getI18NFacade().getRegionsForCountryIso(addressForm.getCountryIso()));
		//			model.addAttribute("country", addressForm.getCountryIso());
		//			storeCmsPageInModel(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));
		//			setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));
		//			//			return ControllerConstants.Views.Pages.Account.AccountEditAddressPage;
		//			return ControllerConstants.Views.Pages.Account.AccountChinaEditAddressPage;
		//
		//		}

		userFacade.addAddress(newAddress);

		return REDIRECT_TO_ADDRESS_BOOK_PAGE;
	}


	@Override
	@RequestMapping(value = "/edit-address/" + ADDRESS_CODE_PATH_VARIABLE_PATTERN, method = RequestMethod.GET)
	@RequireHardLogIn
	public String editAddress(@PathVariable("addressCode") final String addressCode, final Model model)
			throws CMSItemNotFoundException
	{
		final ChinaAddressForm addressForm = new ChinaAddressForm();

		model.addAttribute("formActionURL", "edit-address/" + addressCode);

		model.addAttribute("countryData", checkoutFacade.getDeliveryCountries());
		model.addAttribute("titleData", userFacade.getTitles());
		model.addAttribute("addressForm", addressForm);
		model.addAttribute("addressBookEmpty", Boolean.valueOf(userFacade.isAddressBookEmpty()));

		for (final AddressData addressData : userFacade.getAddressBook())
		{
			if (addressData.getId() != null && addressData.getId().equals(addressCode))
			{
				model.addAttribute("regions", getI18NFacade().getRegionsForCountryIso(addressData.getCountry().getIsocode()));
				model.addAttribute("country", addressData.getCountry().getIsocode());
				model.addAttribute("addressData", addressData);
				addressForm.setAddressId(addressData.getId());
				addressForm.setFirstName(addressData.getFirstName());
				addressForm.setLine1(addressData.getLine1());
				addressForm.setPostcode(addressData.getPostalCode());
				addressForm.setCountryIso(addressData.getCountry().getIsocode());
				if (addressData.getRegion() != null && !StringUtils.isEmpty(addressData.getRegion().getIsocode()))
				{
					addressForm.setRegionIso(addressData.getRegion().getIsocode());
				}


				// if region is set, provide city list
				if (addressData.getRegion() != null && !StringUtils.isEmpty(addressData.getRegion().getIsocode()))
				{
					final List<CityData> cityDTOs = getCityFacade().getCitiesByRegionCode(addressData.getRegion().getIsocode());
					model.addAttribute("cities", cityDTOs);
				}

				// if city is set, provide district list
				if (addressData.getCity() != null && !StringUtils.isEmpty(addressData.getCity()))
				{
					final String cityCode = retrieveCityCode(addressData.getId());

					final List<DistrictData> cityDistricts = this.getDistrictFacade().getDistrictsByCityCode(cityCode);
					model.addAttribute("cityDistricts", cityDistricts);
					addressForm.setCityCode(cityCode);
				}

				// set districtcode if available
				if (addressData.getCityDistrict() != null && !StringUtils.isEmpty(addressData.getCityDistrict()))
				{
					final String cityDistrictCode = retrieveCityDistrictCode(addressData.getId());
					addressForm.setCityDistrictCode(cityDistrictCode);
				}

				// phone // complete phonenumber
				if (addressData.getPhone() != null && !StringUtils.isEmpty(addressData.getPhone()))
				{
					addressForm.setPhone(addressData.getPhone());
				}
				// store phone
				if (addressData.getLandlinePhonePart1() != null && !StringUtils.isEmpty(addressData.getLandlinePhonePart1()))
				{
					addressForm.setLandlinePhonePart1(addressData.getLandlinePhonePart1());
				}
				if (addressData.getLandlinePhonePart2() != null && !StringUtils.isEmpty(addressData.getLandlinePhonePart2()))
				{
					addressForm.setLandlinePhonePart2(addressData.getLandlinePhonePart2());
				}
				if (addressData.getLandlinePhonePart3() != null && !StringUtils.isEmpty(addressData.getLandlinePhonePart3()))
				{
					addressForm.setLandlinePhonePart3(addressData.getLandlinePhonePart3());
				}

				if (addressData.getCellphone() != null && !StringUtils.isEmpty(addressData.getCellphone()))
				{
					addressForm.setCellPhone(addressData.getCellphone());
				}

				//


				if (isDefaultAddress(addressData.getId()))
				{
					addressForm.setDefaultAddress(Boolean.TRUE);
					model.addAttribute("isDefaultAddress", Boolean.TRUE);
				}
				else
				{
					addressForm.setDefaultAddress(Boolean.FALSE);
					model.addAttribute("isDefaultAddress", Boolean.FALSE);
				}
				break;
			}
		}

		storeCmsPageInModel(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));

		final List<Breadcrumb> breadcrumbs = accountBreadcrumbBuilder.getBreadcrumbs(null);
		breadcrumbs.add(new Breadcrumb("/my-account/address-book", getMessageSource().getMessage("text.account.addressBook", null,
				getI18nService().getCurrentLocale()), null));
		breadcrumbs.add(new Breadcrumb("#", getMessageSource().getMessage("text.account.addressBook.addEditAddress", null,
				getI18nService().getCurrentLocale()), null));
		model.addAttribute("breadcrumbs", breadcrumbs);
		model.addAttribute("metaRobots", "no-index,no-follow");
		model.addAttribute("edit", Boolean.TRUE);
		return ControllerConstants.Views.Pages.Account.AccountChinaEditAddressPage;
	}


	@RequestMapping(value = "/edit-address/" + ADDRESS_CODE_PATH_VARIABLE_PATTERN, method = RequestMethod.POST)
	@RequireHardLogIn
	public String editAddress(final ChinaAddressForm addressForm, final BindingResult bindingResult, final Model model,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{

		//		getAddressValidator().validate(addressForm, bindingResult);
		//		if (bindingResult.hasErrors())
		//		{
		//			GlobalMessages.addErrorMessage(model, "form.global.error");
		//			storeCmsPageInModel(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));
		//			setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));
		//			setUpAddressFormAfterError(addressForm, model);
		//			//			return ControllerConstants.Views.Pages.Account.AccountEditAddressPage;
		//			return ControllerConstants.Views.Pages.Account.AccountChinaEditAddressPage;
		//
		//		}

		model.addAttribute("metaRobots", "no-index,no-follow");

		final AddressData newAddress = new AddressData();
		newAddress.setId(addressForm.getAddressId());
		newAddress.setFirstName(addressForm.getFirstName());
		newAddress.setLine1(addressForm.getLine1());
		newAddress.setPostalCode(addressForm.getPostcode());
		newAddress.setShippingAddress(true);
		newAddress.setVisibleInAddressBook(true);

		addressForm.setCountryIso("CN");

		newAddress.setCountry(getI18NFacade().getCountryForIsocode(addressForm.getCountryIso()));

		if (addressForm.getRegionIso() != null && !StringUtils.isEmpty(addressForm.getRegionIso()))
		{
			newAddress.setRegion(getI18NFacade().getRegion(addressForm.getCountryIso(), addressForm.getRegionIso()));
		}


		// store city and district if any
		if (addressForm.getCityCode() != null && !StringUtils.isEmpty(addressForm.getCityCode()))
		{
			final CityData cityData = this.getCityFacade().getCityForCode(addressForm.getCityCode());
			newAddress.setCityData(cityData);
		}
		if (addressForm.getCityDistrictCode() != null && !StringUtils.isEmpty(addressForm.getCityDistrictCode()))
		{
			final DistrictData districtData = this.getDistrictFacade().getDistrictByCode(addressForm.getCityDistrictCode());
			newAddress.setCityDistrictData(districtData);
		}
		//
		// store phone
		if (addressForm.getLandlinePhonePart1() != null && !StringUtils.isEmpty(addressForm.getLandlinePhonePart1()))
		{
			newAddress.setLandlinePhonePart1(addressForm.getLandlinePhonePart1());
		}
		if (addressForm.getLandlinePhonePart2() != null && !StringUtils.isEmpty(addressForm.getLandlinePhonePart2()))
		{
			newAddress.setLandlinePhonePart2(addressForm.getLandlinePhonePart2());
		}
		if (addressForm.getLandlinePhonePart3() != null && !StringUtils.isEmpty(addressForm.getLandlinePhonePart3()))
		{
			newAddress.setLandlinePhonePart3(addressForm.getLandlinePhonePart3());
		}

		if (addressForm.getCellPhone() != null && !StringUtils.isEmpty(addressForm.getCellPhone()))
		{
			newAddress.setCellphone(addressForm.getCellPhone());
		}
		// 


		if (Boolean.TRUE.equals(addressForm.getDefaultAddress()) || userFacade.getAddressBook().size() <= 1)
		{
			newAddress.setDefaultAddress(true);
			newAddress.setVisibleInAddressBook(true);
		}

		//		final AddressVerificationResult<AddressVerificationDecision> verificationResult = getAddressVerificationFacade()
		//				.verifyAddressData(newAddress);
		//		final boolean addressRequiresReview = getAddressVerificationResultHandler().handleResult(verificationResult, newAddress,
		//				model, redirectModel, bindingResult, getAddressVerificationFacade().isCustomerAllowedToIgnoreAddressSuggestions(),
		//				"checkout.multi.address.updated");
		//
		//		if (addressRequiresReview)
		//		{
		//			model.addAttribute("regions", getI18NFacade().getRegionsForCountryIso(addressForm.getCountryIso()));
		//			model.addAttribute("country", addressForm.getCountryIso());
		//			model.addAttribute("edit", Boolean.TRUE);
		//			storeCmsPageInModel(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));
		//			setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));
		//			return ControllerConstants.Views.Pages.Account.AccountEditAddressPage;
		//		}

		userFacade.editAddress(newAddress);

		return REDIRECT_TO_ADDRESS_BOOK_PAGE;
	}



	@RequestMapping(value = "/select-address-location", method = RequestMethod.POST)
	@RequireHardLogIn
	public String selectAddressLocation(final ChinaAddressForm incomingAddressForm, final Model model,
			@RequestParam(value = "cmd", required = false) final String cmd,
			@RequestParam(value = "url", required = false) final String url) throws CMSItemNotFoundException
	{

		if (url != null)
		{
			model.addAttribute("formActionURL", url.trim());
		}


		// add location data for provinces
		model.addAttribute("regions", getI18NFacade().getRegionsForCountryIso("CN"));

		// add location data city if requested
		if (incomingAddressForm.getRegionIso() != null)
		{
			final List<CityData> cityDTOs = getCityFacade().getCitiesByRegionCode(incomingAddressForm.getRegionIso());
			model.addAttribute("cities", cityDTOs);
		}

		// add location data district if requested
		if (incomingAddressForm.getCityCode() != null && cmd.equals("cityselected"))
		{
			final List<DistrictData> cityDistricts = this.getDistrictFacade().getDistrictsByCityCode(
					incomingAddressForm.getCityCode());
			model.addAttribute("cityDistricts", cityDistricts);
		}


		// add incoming address form field values back into returning address form
		final ChinaAddressForm addressForm = new ChinaAddressForm();

		if (incomingAddressForm.getFirstName() != null)
		{
			addressForm.setFirstName(incomingAddressForm.getFirstName());
		}
		if (incomingAddressForm.getLine1() != null)
		{
			addressForm.setLine1(incomingAddressForm.getLine1());
		}
		if (incomingAddressForm.getPostcode() != null)
		{
			addressForm.setPostcode(incomingAddressForm.getPostcode());
		}

		// set incoming values if any for having selectors preselected 
		if (incomingAddressForm.getRegionIso() != null)
		{
			addressForm.setRegionIso(incomingAddressForm.getRegionIso());
		}
		if (incomingAddressForm.getCityCode() != null && cmd.equals("cityselected"))
		{
			addressForm.setCityCode(incomingAddressForm.getCityCode());
		}

		if (incomingAddressForm.getLandlinePhonePart1() != null
				&& !StringUtils.isEmpty(incomingAddressForm.getLandlinePhonePart1()))
		{
			addressForm.setLandlinePhonePart1(incomingAddressForm.getLandlinePhonePart1());
		}
		if (incomingAddressForm.getLandlinePhonePart2() != null
				&& !StringUtils.isEmpty(incomingAddressForm.getLandlinePhonePart2()))
		{
			addressForm.setLandlinePhonePart2(incomingAddressForm.getLandlinePhonePart2());
		}
		if (incomingAddressForm.getLandlinePhonePart3() != null
				&& !StringUtils.isEmpty(incomingAddressForm.getLandlinePhonePart3()))
		{
			addressForm.setLandlinePhonePart3(incomingAddressForm.getLandlinePhonePart3());
		}
		if (incomingAddressForm.getCellPhone() != null && !StringUtils.isEmpty(incomingAddressForm.getCellPhone()))
		{
			addressForm.setCellPhone(incomingAddressForm.getCellPhone());
		}
		if (incomingAddressForm.getAddressId() != null && !StringUtils.isEmpty(incomingAddressForm.getAddressId()))
		{
			addressForm.setAddressId(incomingAddressForm.getAddressId());
		}

		// add required data
		addressForm.setCountryIso("CN");

		model.addAttribute("addressForm", addressForm);
		model.addAttribute("addressBookEmpty", Boolean.valueOf(userFacade.isAddressBookEmpty()));
		model.addAttribute("isDefaultAddress", Boolean.FALSE);
		storeCmsPageInModel(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));

		final List<Breadcrumb> breadcrumbs = accountBreadcrumbBuilder.getBreadcrumbs(null);
		breadcrumbs.add(new Breadcrumb("/my-account/address-book", getMessageSource().getMessage("text.account.addressBook", null,
				getI18nService().getCurrentLocale()), null));
		breadcrumbs.add(new Breadcrumb("#", getMessageSource().getMessage("text.account.addressBook.addEditAddress", null,
				getI18nService().getCurrentLocale()), null));
		model.addAttribute("breadcrumbs", breadcrumbs);
		model.addAttribute("metaRobots", "no-index,no-follow");

		return ControllerConstants.Views.Pages.Account.AccountChinaEditAddressPage;
	}



	protected String retrieveCityCode(final String addressModelPk)
	{
		if (addressModelPk != null)
		{
			final AddressModel obj = this.modelService.get(PK.parse(addressModelPk));
			if (obj != null && obj.getCity() != null)
			{
				return obj.getCity().getCode();
			}
		}
		LOG.info("Could not retrieve city code. AddressModel PK=" + addressModelPk);
		return "";
	}

	protected String retrieveCityDistrictCode(final String addressModelPk)
	{
		if (addressModelPk != null)
		{
			final AddressModel obj = this.modelService.get(PK.parse(addressModelPk));
			if (obj != null && obj.getCityDistrict() != null)
			{
				return obj.getCityDistrict().getCode();
			}
		}
		LOG.info("Could not retrieve citydistrict code. AddressModel PK=" + addressModelPk);
		return "";
	}
}
