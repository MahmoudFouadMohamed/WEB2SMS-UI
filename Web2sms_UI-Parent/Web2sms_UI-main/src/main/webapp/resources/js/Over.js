$(document).ready(function (e) {

    if (navigator.userAgent.search("MSIE") >= 0) {
        $(function () {
            $('input:radio, input:checkbox').click(function () {
                this.blur();
                this.focus();
            });
        });
    }

    /*
     * $('.tab2') .click( function(e) {
     * 
     * $(".tefa_content_loads_here").empty(); $ .get(
     * "../list/contact_list_page.xhtml", function(data) { $(
     * ".tefa_content_loads_here") .append( data); $(".loading-img") .css(
     * 'display', 'none'); //
     * $('.mmagd_contact_list_aad_new_contact_div').css('display','block');
     * 
     * 
     * });
     */
    /*
     * $('.tab3').click( function(e) {
     * 
     * $(".tefa_content_loads_here").empty();
     * 
     * $.get("../template/template_page.xhtml", function(data) {
     * 
     * $(".tefa_content_loads_here") .append(data);
     * $(".loading-img").css('display', 'none');
     * 
     * }); });
     */
    /*
     * $('.tab4') .click( function(e) {
     * 
     * 
     * });
     */

    /*
     * $('.tab1').click( function(e) { $(".tefa_content_loads_here").empty();
     * $.get("../campaign/Campaigns.xhtml", function(data) {
     * $(".tefa_content_loads_here") .append(data);
     * $(".loading-img").css('display', 'none'); showcampaigntab(); });
     * 
     * });
     */
    /*
     * $('.menubg ul li').click(function(e) { $('.menubg ul
     * li').removeClass('activetab'); $(this).addClass('activetab'); });
     * $('.newtabs ul li').click(function(e) { $('.newtabs ul
     * li').removeClass('activeone'); $(this).addClass('activeone'); });
     */

});

function showcampaigntab() {

    $('.mmagd_2sms_campain_edit_btn').click(function (e) {
        $('#mmagd_2sms_wrapper').css('display', 'none');
        $('.mmagd_2sms_editcampain_content').css('display', 'block');
        $('.tefa_History_Section_container').css('display', 'none');
        $('.mmagd_2sms_currentcampain_content').css('display', 'none');
        $('.mmagd_2sms_addcampain_content').css('display', 'none');
    });

    $('.toclosecampaigns').click(function (e) {
        $('#mmagd_2sms_wrapper').css('display', 'block');
        $('.mmagd_2sms_currentcampain_content').css('display', 'block');
        $('.tefa_History_Section_container').css('display', 'none');
        $('.mmagd_2sms_editcampain_content').css('display', 'none');
        $('.mmagd_2sms_addcampain_content').css('display', 'none');
    });

    $('.clicktocreatecampains').click(function (e) {

        $('#mmagd_2sms_wrapper').css('display', 'none');
        $('.mmagd_2sms_addcampain_content').css('display', 'block');
        $('.tefa_History_Section_container').css('display', 'none');
        $('.mmagd_2sms_currentcampain_content').css('display', 'none');
        $('.mmagd_2sms_editcampain_content').css('display', 'none');

        $('.mmagd_to_save_with_schedule').change(function (e) {

            if ($(this).attr('checked')) {
                $('.mmagd_2sms_campain_date_time').css('display', 'block');
                $('.mmagd_2sms_campain_date_time_2').css('display', 'block');

            } else {
                $('.mmagd_2sms_campain_date_time').css('display', 'none');
                $('.mmagd_2sms_campain_date_time_2').css('display', 'none');
            }

        });

    });
    $('#group_1434836598_3').change(function (e) {
        if ($(this).attr('checked')) {
            $('.mmagd_2sms_campain_select').css('display', 'block');
        } else {
            $('.mmagd_2sms_campain_select').css('display', 'none');
        }
    });
    $('#group_1434836598_4').change(function (e) {
        if ($(this).attr('checked')) {
            $('.mmagd_2sms_campain_select_2').css('display', 'block');
        } else {
            $('.mmagd_2sms_campain_select_2').css('display', 'none');
        }
    });
    $('#group_1434836598_5').change(
            function (e) {
                if ($(this).attr('checked')) {
                    $('.mmagd_2sms_campain_template_options_history').css(
                            'display', 'block');
                } else {
                    $('.mmagd_2sms_campain_template_options_history').css(
                            'display', 'none');
                }
            });

    // alert('ss')
    $('.mmagd_to_send_once').change(function (e) {

        if ($(this).attr('checked')) {
            $('.mmagd_2sms_campain_text_field_2').css('display', 'none');
            $('.mmagd_2sms_campain_radio_btn_').css('display', 'none');

        } else {
            $('.mmagd_2sms_campain_text_field_2').css('display', 'block');
            $('.mmagd_2sms_campain_radio_btn_').css('display', 'block');
        }

    });
    // $('.tefa_History_Section_container h3 a').click(function(e) {
    // alert('ff');
    // $('.tefa_campaign_checklist_to_delete label').css('display', 'block');
    //
    // });
    $('.mmagd_2sms_campainHistory').click(function (e) {
        $('.mmagd_2sms_addcampain_content').css('display', 'none');
        $('.mmagd_2sms_currentcampain_content').css('display', 'none');
        $('.mmagd_2sms_editcampain_content').css('display', 'none');
        $('.tefa_History_Section_container').css('display', 'block');
    });
}

function showOverlay(overlay) {

    var ele = eval(overlay);
    var widt;
    var heig;
    if (document.body && document.body.offsetWidth) {
        widt = document.body.offsetWidth;
        heig = document.body.offsetHeight;
    }
    if (document.compatMode === 'CSS1Compat' && document.documentElement
            && document.documentElement.offsetWidth) {
        widt = document.documentElement.offsetWidth;
        heig = document.documentElement.offsetHeight;
    }
    if (window.innerWidth && window.innerHeight) {
        widt = window.innerWidth;
        heig = window.innerHeight;
    }
    $('.overlayBG').animate({
        opacity: .6
    }, 250, function () {
    });
    $('.overlayBG').css('display', 'block');
    $('.overlayBG').css('z-index', 999999);

    $(ele).animate({
        opacity: 1
    }, 250, function () {
    });
    $(ele).css('width', widt);
    $(ele).css('height', heig);
    $(ele).css('z-index', 9999991);
    $(ele).css('display', 'block');

    var thispos = $('.overlayContent').width();
    var xpos = (widt - 800) / 2;
    $('.overlayContent').css('left', xpos);
    $('.modalDetails').css('display', 'block');
}
function hideoverlay(overlay) {
    var ele = eval(overlay);
    $(ele).animate({
        opacity: 0
    }, 250, function () {
        $(ele).css('z-index', -100);
        $(ele).css('display', 'none');
    });
    $('.overlayBG').animate({
        opacity: 0
    }, 250, function () {
    });
    $('.overlayBG').css('display', 'none');
    $('.overlayBG').css('z-index', -100);

}

function hideitem1(ele) {
    var myElement = $(ele).parent();
    $(myElement).css('display', 'none');
}

function hideitem2() {

    $('#mmagd_2sms_item_2').css('display', 'none');

}

function hideitem3() {

    $('#mmagd_2sms_item_3').css('display', 'none');

}

function getId(ele) {
    var x = $(ele).prev().attr('id');
    $('.list_label_test').attr('for', x);
}

function editTest(data) {
    $('#mmagd_2sms_wrapper').css('display', 'none');
    $('.mmagd_2sms_editcampain_content').css('display', 'block');
    $('.tefa_History_Section_container').css('display', 'none');
    $('.mmagd_2sms_currentcampain_content').css('display', 'none');
    $('.mmagd_2sms_addcampain_content').css('display', 'none');
    updateCountdownEdit();

}

function hideBlankList() {
    $('.mmagd_SubList').css('display', 'block');
    $('.mmagd_contact_list_aad_new_contact_div').css('display', 'none');
    $('.mmagd_contact_list_aad_new_contact_div2').css('display', 'none');
    $('.mmagd_2sms_contact_list_asset_contacts').css('display', 'none');
    /* clickMyButton(); */
}

function getMappingcontactId(ele, columnIndex) {
    var x = $(ele).attr('id');
    var y = columnIndex;
    var value_1 = $(ele).val();
    if (value_1 == "name") {
        var input = document.getElementById('uploadForm:test-name-map');
        input.value = y;
        var mobile = document.getElementById('uploadForm:test-msisdn-map');
        var value1 = document.getElementById('uploadForm:test-value1-map');
        var value2 = document.getElementById('uploadForm:test-value2-map');
        var value3 = document.getElementById('uploadForm:test-value3-map');
        var value4 = document.getElementById('uploadForm:test-value4-map');
        var value5 = document.getElementById('uploadForm:test-value5-map');

        if (mobile.value == input.value)
            mobile.value = "-1";
        if (value1.value == input.value)
            value1.value = "-1";
        if (value2.value == input.value)
            value2.value = "-1";
        if (value3.value == input.value)
            value3.value = "-1";
        if (value4.value == input.value)
            value4.value = "-1";
        if (value5.value == input.value)
            value5.value = "-1";
    } else if (value_1 == "mobile") {
        var input = document.getElementById('uploadForm:test-msisdn-map');
        input.value = y;
        var name = document.getElementById('uploadForm:test-name-map');
        var value1 = document.getElementById('uploadForm:test-value1-map');
        var value2 = document.getElementById('uploadForm:test-value2-map');
        var value3 = document.getElementById('uploadForm:test-value3-map');
        var value4 = document.getElementById('uploadForm:test-value4-map');
        var value5 = document.getElementById('uploadForm:test-value5-map');

        if (name.value == input.value)
            name.value = "-1";
        if (value1.value == input.value)
            value1.value = "-1";
        if (value2.value == input.value)
            value2.value = "-1";
        if (value3.value == input.value)
            value3.value = "-1";
        if (value4.value == input.value)
            value4.value = "-1";
        if (value5.value == input.value)
            value5.value = "-1";
    }
    else if (value_1 == "Value1") {
        var input = document.getElementById('uploadForm:test-value1-map');
        input.value = y;
        var mobile = document.getElementById('uploadForm:test-msisdn-map');
        var name = document.getElementById('uploadForm:test-name-map');
        var value2 = document.getElementById('uploadForm:test-value2-map');
        var value3 = document.getElementById('uploadForm:test-value3-map');
        var value4 = document.getElementById('uploadForm:test-value4-map');
        var value5 = document.getElementById('uploadForm:test-value5-map');

        if (mobile.value == input.value)
            mobile.value = "-1";
        if (name.value == input.value)
            name.value = "-1";
        if (value2.value == input.value)
            value2.value = "-1";
        if (value3.value == input.value)
            value3.value = "-1";
        if (value4.value == input.value)
            value4.value = "-1";
        if (value5.value == input.value)
            value5.value = "-1";
    }
    else if (value_1 == "Value2") {
        var input = document.getElementById('uploadForm:test-value2-map');
        input.value = y;
        var mobile = document.getElementById('uploadForm:test-msisdn-map');
        var value1 = document.getElementById('uploadForm:test-value1-map');
        var name = document.getElementById('uploadForm:test-name-map');
        var value3 = document.getElementById('uploadForm:test-value3-map');
        var value4 = document.getElementById('uploadForm:test-value4-map');
        var value5 = document.getElementById('uploadForm:test-value5-map');

        if (mobile.value == input.value)
            mobile.value = "-1";
        if (value1.value == input.value)
            value1.value = "-1";
        if (name.value == input.value)
            name.value = "-1";
        if (value3.value == input.value)
            value3.value = "-1";
        if (value4.value == input.value)
            value4.value = "-1";
        if (value5.value == input.value)
            value5.value = "-1";
    }
    else if (value_1 == "Value3") {
        var input = document.getElementById('uploadForm:test-value3-map');
        input.value = y;
        var mobile = document.getElementById('uploadForm:test-msisdn-map');
        var value1 = document.getElementById('uploadForm:test-value1-map');
        var value2 = document.getElementById('uploadForm:test-value2-map');
        var name = document.getElementById('uploadForm:test-name-map');
        var value4 = document.getElementById('uploadForm:test-value4-map');
        var value5 = document.getElementById('uploadForm:test-value5-map');

        if (mobile.value == input.value)
            mobile.value = "-1";
        if (value1.value == input.value)
            value1.value = "-1";
        if (value2.value == input.value)
            value2.value = "-1";
        if (name.value == input.value)
            name.value = "-1";
        if (value4.value == input.value)
            value4.value = "-1";
        if (value5.value == input.value)
            value5.value = "-1";
    }
    else if (value_1 == "Value4") {
        var input = document.getElementById('uploadForm:test-value4-map');
        input.value = y;
        var mobile = document.getElementById('uploadForm:test-msisdn-map');
        var value1 = document.getElementById('uploadForm:test-value1-map');
        var value2 = document.getElementById('uploadForm:test-value2-map');
        var value3 = document.getElementById('uploadForm:test-value3-map');
        var name = document.getElementById('uploadForm:test-name-map');
        var value5 = document.getElementById('uploadForm:test-value5-map');

        if (mobile.value == input.value)
            mobile.value = "-1";
        if (value1.value == input.value)
            value1.value = "-1";
        if (value2.value == input.value)
            value2.value = "-1";
        if (value3.value == input.value)
            value3.value = "-1";
        if (name.value == input.value)
            name.value = "-1";
        if (value5.value == input.value)
            value5.value = "-1";
    }
    else if (value_1 == "Value5") {
        var input = document.getElementById('uploadForm:test-value5-map');
        input.value = y;
        var mobile = document.getElementById('uploadForm:test-msisdn-map');
        var value1 = document.getElementById('uploadForm:test-value1-map');
        var value2 = document.getElementById('uploadForm:test-value2-map');
        var value3 = document.getElementById('uploadForm:test-value3-map');
        var value4 = document.getElementById('uploadForm:test-value4-map');
        var name = document.getElementById('uploadForm:test-name-map');

        if (mobile.value == input.value)
            mobile.value = "-1";
        if (value1.value == input.value)
            value1.value = "-1";
        if (value2.value == input.value)
            value2.value = "-1";
        if (value3.value == input.value)
            value3.value = "-1";
        if (value4.value == input.value)
            value4.value = "-1";
        if (name.value == input.value)
            name.value = "-1";
    }

    var hidden = [];
    // Get the values that should be hidden
    $('select.mapType').each(function () {
        var val = $(this).find('option:selected').val();
        if (val != "none") {
            hidden.push($(this).find('option:selected').val());
        }
    });
    // Show all options...
    $('select.mapType option').show().removeAttr('disabled');
    // ...and hide those that should be invisible
    for (var i in hidden) {
        // Note the not(':selected'); we don't want to hide the option from
        // where
        // it's active. The hidden option should also be disabled to prevent it
        // from submitting accidentally (just in case).
        $('select.mapType option[value=' + hidden[i] + ']').not(':selected')
                .hide().attr('disabled', 'disabled');
    }
}

function resizeIframettt() {
    var height = $('#records-view-table').height();
    $('.upload-iframe').css('height', 230 + height + "px");
    alert(height);
}

function changeFakeFile() {
    var x = $('.file-real').val();
    $('.replace-upload').val(x);
}

function changeUploadType() {
    $('.replace-upload').val('');
}

function clickOnUpdate(ele) {
    var x = $(ele);
    $(x).next().click();
    $('.mmagd_SubList').css('display', 'block');
    $('.mmagdinnergroup').css('display', 'block');
    $('.mmagd_2sms_contact_list_list_view').css('width', 50 + '%');
    $('.full_width').css('width', 429 + "px");
    if ($('.mmagd_2sms_conatct_list_search_icon').css('direction') === 'rtl')
        $('.mmagd_2sms_conatct_list_search_icon').css('right', 420 + "px");
    else
        $('.mmagd_2sms_conatct_list_search_icon').css('right', 4 + "px");
    $('.sub_list').css('right', 12 + "px");
    $('.sub_list').css('top', 40 + "px");
    $('.mmagd_2sms_conatct_list_check_list_conatct').css('float', 'left');
    $('.mmagd_contact_list_aad_new_contact_div').css('display', 'none');
    $('.mmagd_contact_list_aad_new_contact_div2').css('display', 'none');
}

function showSideForm(ele) {
	var x = $(ele);
	$(x).next().click();
	$(".mmagd_SubList").css("display", "block");
	$(".mmagdinnergroup").css("display", "block");
	$(".mmagd_2sms_contact_list_list_view").css("width", 55 + "%");
	$(".full_width").css("width", 95 + "%");
        if($(".mmagd_2sms_conatct_list_search_icon").css("direction") === "rtl")
            $(".mmagd_2sms_conatct_list_search_icon").css("right", 490 + "px");
        else
            $(".mmagd_2sms_conatct_list_search_icon").css("right", 7 + "px");
	$(".sub_list").css("right", 12 + "px");
	$(".sub_list").css("top", 40 + "px");
	$(".mmagd_2sms_conatct_list_check_list_conatct").css("float", "left");
	$(".mmagd_contact_list_aad_new_contact_div").css("display", "none");
	$(".mmagd_contact_list_aad_new_contact_div2").css("display", "none");
}


function hideSideForm(ele) {
	var x = $(ele);
	$(x).next().click();
	$(".mmagd_SubList").css("display", "block");
	$(".mmagd_contact_list_aad_new_contact_div").css("display", "none");
	$(".mmagd_contact_list_aad_new_contact_div2").css("display", "none");
	$(".mmagd_2sms_contact_list_asset_contacts").css("display", "none");
	$(".mmagd_2sms_contact_list_list_view").css("width", 100 + "%");
	$(".full_width").css("width",  97 + "%");
        if($(".mmagd_2sms_conatct_list_search_icon").css("direction") === "rtl")
            $(".mmagd_2sms_conatct_list_search_icon").css("right", 890 + "px");
        else
            $(".mmagd_2sms_conatct_list_search_icon").css("right", 10 + "px");
	/* clickMyButton(); */
}

function showMyDiv() {
    // if(${createListBean.showInvalidFile}){
    showOverlay('payment_methods_overlay_7');
    // }
}

function tempTextFocus(ele) {
    var x = $(ele);
    $(x).parent().parent().next().next().next().css('display', 'block');
    $(x).parent().parent().parent().parent().addClass('active');
    $(x).parent().parent().next().removeClass('textareaDis');
    $(x).parent().parent().next().removeAttr('readonly');
    $(x).css('display', 'none');
    $(x).next().css('display', 'block');
}

function hideTempCreate() {
    $(".mmagd_2sms_emplate_data_erea").css('display', 'none');
    $(".mmagd_2sms_template_erea_name")
            .attr('style', 'display:none !important');
}

function testSave() {
    $('.buttontosavetemp').css('display', 'none');
}

function hideEditTemp(ele) {
    var x = $(ele);
    $(x).parent().parent().css('display', 'none');
    $('.refreshTemp').click();
}

function deleteThisTemp(ele) {
    var x = $(ele);
    $(x).parent().parent().css('display', 'none');
    $(x).parent().parent().parent().css('display', 'none');
}

function hideoverlay2(overlay) {
    var ele = eval(overlay);
    $(ele).animate({
        opacity: 0
    }, 250, function () {
        $(ele).css('z-index', -100);
        $(ele).css('display', 'none');
    });
    $('.overlayBG').animate({
        opacity: 0
    }, 250, function () {
    });
    $('.overlayBG').css('display', 'none');
    $('.overlayBG').css('z-index', -100);
    $('.mmagd_SubList').css('display', 'none');

}

function clickMyButton() {
    $('.loadButton5').click();
}

function hideWrapper() {
    $('#mmagd_2sms_wrapper').css('display', 'none');
}

function hideCustomoverlay(overlay) {
    var ele = eval(overlay);
    $(ele).animate({
        opacity: 0
    }, 250, function () {
        $(ele).css('z-index', -100);
        $(ele).css('display', 'none');
    });
}

function hideEditTemp1(data) {
    if (data.status == "complete") {
        $('.refreshTemp').click();
    }
}

function showAddNewContact() {

    $('.mmagd_SubList').css('display', 'none');
    $('.mmagd_contact_list_aad_new_contact_div2').css('display', 'block');
}

function Animate_progressbar(id, value) {
    var Complete_ID;
    Complete_ID = "#" + id;
    $(Complete_ID)
            .animate(
                    {
                        width: value + "%"
                    },
            {
                easing: 'linear',
                duration: 1000,
                step: function (current_width) {
                    if (current_width <= 50) {
                        $(Complete_ID).css('background-color',
                                '#efac33');
                        $(Complete_ID).parent().parent().css('border',
                                'solid 1px #efac33');
                        $(Complete_ID).find(".Loading_Bar_Rulers").css(
                                'background-position', '0px -480px');
                        $(Complete_ID).find(
                                ".UBD_Small_Progress_Bar_txt").css(
                                '-webkit-text-stroke-color', '#efac33');
                    } else if (current_width > 50
                            && current_width <= 75) {
                        $(Complete_ID).css('background-color',
                                '#efab33');
                        $(Complete_ID).parent().parent().css('border',
                                'solid 1px #efab33');
                        $(Complete_ID).find(".Loading_Bar_Rulers").css(
                                'background-position', '0px -491px');
                        $(Complete_ID).find(
                                ".UBD_Small_Progress_Bar_txt").css(
                                '-webkit-text-stroke-color', '#efab33');

                    } else if (current_width > 75
                            && current_width <= 100) {
                        $(Complete_ID).css('background-color',
                                '#e60000');
                        $(Complete_ID).find(".Loading_Bar_Rulers").css(
                                'background-position', '0px -502px');
                        $(Complete_ID).find(
                                ".UBD_Small_Progress_Bar_txt").css(
                                '-webkit-text-stroke-color', '#e60000');
                        if (current_width == 100) {
                            $(Complete_ID).find(".Loading_Bar_Rulers")
                                    .css('background-position',
                                            '0px -502px');
                            $(Complete_ID).parent().parent().css(
                                    'border', 'solid 1px #e60000');
                            $(Complete_ID).find(
                                    ".UBD_Small_Progress_Bar_txt").css(
                                    '-webkit-text-stroke-color',
                                    '#e60000');
                        }
                    }
                }
            });
}
function changeLanguage(lang) {
    window.localStorage.setItem("language", lang);
}
function getLanguage() {
    if(window.localStorage.getItem("language")===null)
        changeLanguage('en');
    return window.localStorage.getItem("language");
}
function getCookie(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}