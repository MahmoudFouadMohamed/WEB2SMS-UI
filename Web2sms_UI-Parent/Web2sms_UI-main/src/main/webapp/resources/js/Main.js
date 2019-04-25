$(document).ready(function() {
	//showOverlay('VFRed_Overlay_Change_rate_plan');
	Animate_progressbar('progressbar', 100);
	Animate_progressbar('progressbar1', 20);
	Animate_progressbar('progressbar2', 75);
	Animate_progressbar('progressbar3', 75);
	Animate_progressbar('progressbar4', 100);
	Animate_progressbar('progressbar5', 20);
	Animate_progressbar('progressbar6', 75);
	responsiveScript();
	$('.VFRed_Main_HeroBanner_Carousel').slick({
			centerMode: true,
  			centerPadding: '60px',
  			slidesToShow: 6,
  			responsive: [
    		{
     			 breakpoint: 1024,
      			 settings: {
				 autoplay: true,
        		 arrows: true,
        		 centerMode: true,
        		 centerPadding: '40px',
        		 slidesToShow: 3
      		}
    		},
    		{
     			 breakpoint: 500,
      			 settings: {
				 autoplay: true,
				 autoplaySpeed: 2000,
        		 arrows: true,
        		 centerMode: true,
        		 centerPadding: '40px',
        		 slidesToShow: 1
      		}
			}
  		]
  	});	
});

$(window).resize(function() {
	responsiveScript();
});

function responsiveScript(){
	if (document.documentElement.clientWidth >= 0 && document.documentElement.clientWidth <= 639) {
		$(".Post_Taps").click(function() {
			$(this).addClass('ActiveTaps');
			$('.Pre_Taps').removeClass('ActiveTaps');
			$('.VFRed_Post_Paid').css('display','block');
			$('.VFRed_Pre_Paid').css('display','none');
		});
		$(".Pre_Taps").click(function() {
			$(this).addClass('ActiveTaps');
			$('.Post_Taps').removeClass('ActiveTaps');
			$('.VFRed_Post_Paid').css('display','none');
			$('.VFRed_Pre_Paid').css('display','block');
		});
		$(".VFRed_Options_Header").click(function() {
			$('.VFRed_Options_Main').css('display','none');
			$(this).parent().find('.VFRed_Options_Main').css('display','block');
			$(this).parent().find('.VFRed_Options_Header_Arrow_Icon').css('background-position','-80px -18px');		
		});
		$(".ViewInformationPage , .Show_Subscribe").click(function() {
			$('.VFRed_Container').css('display','none');
			$('.overlaydiv').css({'width':'100%' , 'height' : 'auto' , 'float' : 'left' , 'padding' : '1%'});		
		});
		$(".BTN_Overlay_Smart_Phone").click(function() {
			$('.VFRed_Container').css('display','block');
			$('.overlaydiv').css('display','none');
		});
		$(".Show_Enter_Mobile_Number").click(function() {
			$('.VFRed_EnterMobileNumber').css('display','block');
		});
		$("#VFRed_Send_SMS").click(function() {
			$('.VFRed_EnterYourCode').css('display','block');
			$('.VFRed_EnterMobileNumber').css('display','none');
		});
		$("#VFRed_Send_SMS_Again").click(function() {
			$('.VFRed_EnterYourCode').css('display','none');
			$('.VFRed_EnterMobileNumber').css('display','block');
		});	
	}
	if (document.documentElement.clientWidth >= 640 && document.documentElement.clientWidth <= 1023) {
		//VFRed_Carousel()
			$(".Post_Taps").click(function() {
			$(this).addClass('ActiveTaps');
			$('.Pre_Taps').removeClass('ActiveTaps');
			$('.VFRed_Post_Paid').css('display','block');
			$('.VFRed_Pre_Paid').css('display','none');
		});
		$(".Pre_Taps").click(function() {
			$(this).addClass('ActiveTaps');
			$('.Post_Taps').removeClass('ActiveTaps');
			$('.VFRed_Post_Paid').css('display','none');
			$('.VFRed_Pre_Paid').css('display','block');
		});	
	}
	if (document.documentElement.clientWidth >= 1024) {
		$(".Post_Taps").click(function() {
			$(this).addClass('ActiveTaps');
			$('.Pre_Taps').removeClass('ActiveTaps');
			$('.VFRed_Post_Paid').css('display','block');
			$('.VFRed_Pre_Paid').css('display','none');
			$('html,body').animate({
				scrollTop : 650
			}, 1000);
		});
		$(".Pre_Taps").click(function() {
			$(this).addClass('ActiveTaps');
			$('.Post_Taps').removeClass('ActiveTaps');
			$('.VFRed_Post_Paid').css('display','none');
			$('.VFRed_Pre_Paid').css('display','block');
			$('html,body').animate({
				scrollTop : 650
			}, 1000);
		});
		$(".Show_Enter_Mobile_Number").click(function() {
			$('.VFRed_EnterMobileNumber').css('display','block');
		});
		$("#VFRed_Send_SMS").click(function() {
			$('.VFRed_EnterYourCode').css('display','block');
			$('.VFRed_EnterMobileNumber').css('display','none');
		});
		$("#VFRed_Send_SMS_Again").click(function() {
			$('.VFRed_EnterYourCode').css('display','none');
			$('.VFRed_EnterMobileNumber').css('display','block');
		});
	}
	}
		function showOverlay(overlay) {
		var ele = eval(overlay);
		var widt;
		var heig;
		if (document.body && document.body.offsetWidth) {
			widt = document.body.offsetWidth;
			heig = document.body.offsetHeight;
		}
		if (document.compatMode === 'CSS1Compat' && document.documentElement && document.documentElement.offsetWidth) {
			widt = document.documentElement.offsetWidth;
			heig = document.documentElement.offsetHeight;
		}
		if (window.innerWidth && window.innerHeight) {
			widt = window.innerWidth;
			heig = window.innerHeight;
		}
		$(ele).animate({
			opacity : 1
		}, 250, function() {
		});
		$(ele).css('width', widt);
		$(ele).css('height', heig);
		$(ele).css('z-index', 100000);
		$(ele).css('display', 'block');
		var thispos = ($('.overlayContent ').width());
		var xpos = (widt - thispos) / 2;
		$('.overlayContent').css('left', xpos);
	}
	function hideoverlay(overlay) {
		var ele = eval(overlay);
		$(ele).animate({
			opacity : 0
		}, 250, function() {
			$(ele).css('z-index', -100);
			$(ele).css('display', 'none');
		});
	}
	
	
	
	function VFRed_Carousel(){
		$('.VFRed_PostAndPre_Paid').slick({
					 centerMode: true,
  					 //centerPadding: '60px',
 					 slidesToShow: 3,
  				responsive: [
    				{
     				 breakpoint: 768,
      				 settings: {
       					 arrows: false,
        				 centerMode: true,
        				 //centerPadding: '40px',
       					 slidesToShow: 3
      				}
   			    },
    				{
      					 breakpoint: 480,
                           settings: {
                                 arrows: false,
                                 centerMode: true,
                                 //centerPadding: '40px',
                                 slidesToShow: 1
                                     }
                    }
               ]});
	}
	
	function calculate_Data(qouta, consumed) {
	var Qouta;
	var Consumed;
}
function Animate_progressbar(id, value) {
	var Complete_ID;
	Complete_ID = "#" + id;
	$(Complete_ID).animate({
		width : value + "%"
	}, {
		easing : 'linear',
		duration : 10000,
		step : function(current_width) {
			if (current_width <= 50) {
				$(Complete_ID).css('background-color', '#b9c333');
				$(Complete_ID).parent().parent().css('border', 'solid 1px #b9c333');
				$(Complete_ID).find(".Loading_Bar_Rulers").css('background-position', '0px -480px');
				$(Complete_ID).find (".UBD_Small_Progress_Bar_txt").css('-webkit-text-stroke-color', '#b9c333');
			} else if (current_width > 50 && current_width <= 75) {
				$(Complete_ID).css('background-color', '#efab33');
				$(Complete_ID).parent().parent().css('border', 'solid 1px #efab33');
				$(Complete_ID).find (".Loading_Bar_Rulers").css('background-position', '0px -491px');
				$(Complete_ID).find (".UBD_Small_Progress_Bar_txt").css('-webkit-text-stroke-color', '#efab33');
			
			} else if (current_width > 75 && current_width <= 100) {
				$(Complete_ID).css('background-color', '#e60000');
				$(Complete_ID).find(".Loading_Bar_Rulers").css('background-position', '0px -502px');
				$(Complete_ID).find (".UBD_Small_Progress_Bar_txt").css('-webkit-text-stroke-color', '#e60000');
				//$(Complete_ID).parent().parent().css('border', 'solid 1px #e60000');
			if (current_width == 100) {
				$(Complete_ID).find(".Loading_Bar_Rulers").css('background-position', '0px -502px');
				$(Complete_ID).parent().parent().css('border', 'solid 1px #e60000');
				$(Complete_ID).find (".UBD_Small_Progress_Bar_txt").css('-webkit-text-stroke-color', '#e60000');
				}
			}
		}
	});
}
	