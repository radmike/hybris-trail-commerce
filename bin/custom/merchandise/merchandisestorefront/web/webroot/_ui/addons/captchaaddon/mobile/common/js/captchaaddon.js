ACCMOB.captcha = {

	bindAll: function ()
	{
		this.renderWidget();
	},

	renderWidget: function ()
	{
		$.ajax({
			url: ACCMOB.config.contextPath + "/register/captcha/widget/recaptcha",
			type: 'GET',
			success: function (html)
			{
				if ($(html) != [])
				{
					$('ul#registerFormList.mFormList li:last').before($(html));
					$('ul#registerFormList.mFormList').find('div[data-role="fieldcontain"]').fieldcontain();
					$("#recaptcha_response_field").textinput();

					if ($('#recaptchaChallangeAnswered').val() == 'false')
					{
						$('#captcha_error').addClass('form_field_error');
					}

					ACCMOB.captcha.bindCaptcha();
				}
			}
		});
	},
	bindCaptcha: function ()
	{
		$.getScript('https://www.google.com/recaptcha/api/js/recaptcha_ajax.js', function ()
		{
			var publicKey = $('#recaptcha_widget').data('recaptchaPublicKey');
			if (!(typeof publicKey === 'undefined'))
			{
				Recaptcha.create(publicKey, "recaptcha_widget", {
					theme: "custom",
					lang: ACCMOB.config.language
				});
			}
		});
	}
}
$(document).ready(function ()
{
	if ($('#registerForm').html() != null)
	{
		ACCMOB.captcha.bindAll();
	}
});
