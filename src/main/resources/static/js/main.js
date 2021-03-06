$(document).ready(function()
{
    $('#button-logout').click(function()
    {
        document.getElementById('logout-form').submit();
    });

    $('.sidenav').sidenav();

    $('.modal').modal();

    if($("#modalBackupReminder").length)
    {
        $('#modalBackupReminder').modal('open');
    }

    $('.tooltipped').tooltip();

    $('select').formSelect();

    $("#selectWrapper ul.dropdown-content.select-dropdown li span").each(function()
    {
        if($(this).text() === accountPlaceholderName)
        {
            $(this).addClass("all-account-placeholder");
        }
    });

    $("#selectAccount").on('change', function()
    {
        var accountID = $(this).val();
        window.location = rootURL + "/accounts/" + accountID + "/select";
    });

    if($("#login-password").length)
    {
        document.getElementById("login-password").focus();
    }

    $("#buttonSearch").click(function()
    {
        document.getElementById("navbarSearchForm").submit();
    });

    $("#buttonClearSearch").click(function()
    {
        document.getElementById("search").value = "";
    });
});

function addClass(element, className)
{
    if(element != null)
    {
        if(!element.classList.contains(className))
        {
            element.classList.add(className);
        }
    }
}

function removeClass(element, className)
{
    if(element != null)
    {
        if(element.classList.contains(className))
        {
            element.classList.remove(className);
        }
    }
}

function rgb2hex(rgb)
{
    rgb = rgb.match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/);

    function hex(x)
    {
        return ("0" + parseInt(x).toString(16)).slice(-2);
    }

    return "#" + hex(rgb[1]) + hex(rgb[2]) + hex(rgb[3]);
}