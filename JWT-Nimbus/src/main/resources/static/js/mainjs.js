$(function () {
  const tokenKey = 'jwt-demo-token';
  const expiryKey = 'jwt-demo-expires-at';

  function api(path, method, data) {
    const token = localStorage.getItem(tokenKey);
    const options = {
      url: path,
      method: method || 'GET',
      dataType: 'json',
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    };
    if (data !== undefined) {
      options.contentType = 'application/json; charset=utf-8';
      options.data = JSON.stringify(data);
    }
    return $.ajax(options);
  }

  function showApiErrors($form, xhr, fallback) {
    const result = xhr.responseJSON;
    const errors = result && Array.isArray(result.errors) ? result.errors : [];
    const messages = [];
    errors.forEach((item) => {
      const $input = $form.find(`[name="${item.field}"]`);
      if ($input.length) {
        $input.attr('aria-invalid', 'true');
        $form.find(`#${$input.attr('id')}-error`).text(item.message);
      }
      messages.push(item.message);
    });
    const message = messages.join(' ') || (result && result.message) || fallback;
    const $summary = $form.find('.form-message').first();
    $summary.text(message).addClass('error').attr('role', 'alert').attr('aria-live', 'assertive');
    const $invalid = $form.find('[aria-invalid="true"]').first();
    if ($invalid.length) $invalid.trigger('focus');
  }

  function setBusy($button, busy) {
    $button.prop('disabled', busy).toggleClass('is-loading', busy).attr('aria-busy', String(busy));
  }

  function clearFormErrors($form) {
    $form.find('.field-error').empty();
    $form.find('input').removeAttr('aria-invalid');
    $form.find('.form-message').empty().removeClass('error');
  }

  const $loginForm = $('#login-form');
  const $registerForm = $('#register-form');
  const $loginTab = $('#login-tab');
  const $registerTab = $('#register-tab');

  function selectTab(register) {
    $loginTab.toggleClass('is-active', !register).attr('aria-selected', String(!register));
    $registerTab.toggleClass('is-active', register).attr('aria-selected', String(register));
    $('#login-panel').prop('hidden', register);
    $('#register-panel').prop('hidden', !register);
    clearFormErrors($loginForm.add($registerForm));
    (register ? $('#register-username') : $('#login-username')).trigger('focus');
  }

  $loginTab.on('click', () => selectTab(false));
  $registerTab.on('click', () => selectTab(true));
  $loginTab.add($registerTab).on('keydown', function (event) {
    if (event.key === 'ArrowLeft' || event.key === 'ArrowRight') {
      event.preventDefault();
      selectTab(this.id === 'login-tab');
      (this.id === 'login-tab' ? $registerTab : $loginTab).trigger('focus');
    }
  });

  if ($loginForm.length) {
    const flashMessage = sessionStorage.getItem('jwt-demo-message');
    if (flashMessage) {
      $('#login-error').text(flashMessage).addClass('error');
      sessionStorage.removeItem('jwt-demo-message');
    }
    $loginForm.find('input').on('input', function () {
      $(this).removeAttr('aria-invalid');
      $(`#${this.id}-error`).empty();
      $('#login-error').empty().removeClass('error');
    });
    $registerForm.find('input').on('input', function () {
      $(this).removeAttr('aria-invalid');
      $(`#${this.id}-error`).empty();
      $('#register-message').empty().removeClass('error');
    });

    $loginForm.on('submit', function (event) {
      event.preventDefault();
      clearFormErrors($loginForm);
      if (!this.reportValidity()) return;
      const $button = $(this).find('[type="submit"]');
      setBusy($button, true);
      api('/auth/login', 'POST', {
        username: $('#login-username').val(),
        password: $('#login-password').val()
      }).done((result) => {
        localStorage.setItem(tokenKey, result.data.token);
        localStorage.setItem(expiryKey, String(result.data.expiresAt));
        window.location.assign('/user/profile');
      }).fail((xhr) => showApiErrors($loginForm, xhr, 'Không thể đăng nhập. Vui lòng thử lại.'))
        .always(() => setBusy($button, false));
    });

    $registerForm.on('submit', function (event) {
      event.preventDefault();
      clearFormErrors($registerForm);
      if (!this.reportValidity()) return;
      const username = $('#register-username').val();
      const $button = $(this).find('[type="submit"]');
      setBusy($button, true);
      api('/auth/register', 'POST', {
        username,
        password: $('#register-password').val()
      }).done((result) => {
        $('#login-username').val(result.data.username);
        this.reset();
        selectTab(false);
        $('#login-error').text('Tạo tài khoản thành công. Hãy đăng nhập để nhận JWT.')
          .removeClass('error').attr('role', 'status').attr('aria-live', 'polite');
      }).fail((xhr) => showApiErrors($registerForm, xhr, 'Không thể tạo tài khoản. Vui lòng thử lại.'))
        .always(() => setBusy($button, false));
    });
  }

  if ($('#profile-page').length) {
    const token = localStorage.getItem(tokenKey);
    if (!token) {
      sessionStorage.setItem('jwt-demo-message', 'Bạn cần đăng nhập để xem hồ sơ.');
      window.location.replace('/login');
      return;
    }

    api('/users/me').done((result) => {
      const user = result.data;
      $('#profile-username').text(user.username);
      $('#profile-id').text(user.id);
      $('#profile-role').text(user.role);
      const expiresAt = Number(localStorage.getItem(expiryKey));
      $('#token-expiry').text(expiresAt ? new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium', timeStyle: 'short' }).format(expiresAt) : 'Không có thông tin');
      $('#profile-loading').prop('hidden', true);
      $('#profile-details').prop('hidden', false);
      $('#auth-status').text('Bearer token hợp lệ');
      $('#auth-status-dot').removeClass('is-pending');
    }).fail((xhr) => {
      if (xhr.status === 401) {
        localStorage.removeItem(tokenKey);
        localStorage.removeItem(expiryKey);
        sessionStorage.setItem('jwt-demo-message', 'Phiên đăng nhập không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
        window.location.replace('/login');
      } else {
        $('#profile-loading').prop('hidden', true);
        $('#profile-error').text('Không thể tải hồ sơ. Vui lòng thử lại.').addClass('error');
      }
    });

    $('#users-button').on('click', function () {
      const $button = $(this);
      const $message = $('#users-message').empty().removeClass('error');
      $('#users-empty, #users-table-wrap').prop('hidden', true);
      setBusy($button, true);
      api('/users').done((result) => {
        const users = result.data || [];
        const $body = $('#users-body').empty();
        if (!users.length) {
          $('#users-empty').prop('hidden', false);
          $message.text('API trả về danh sách rỗng.');
          return;
        }
        users.forEach((user) => {
          const $row = $('<tr>');
          $('<td>').text(user.id).appendTo($row);
          $('<td>').text(user.username).appendTo($row);
          $('<td>').text(user.role).appendTo($row);
          $body.append($row);
        });
        $('#users-table-wrap').prop('hidden', false);
        $message.text(`Đã tải ${users.length} người dùng.`);
      }).fail((xhr) => {
        if (xhr.status === 401) {
          localStorage.removeItem(tokenKey);
          localStorage.removeItem(expiryKey);
          sessionStorage.setItem('jwt-demo-message', 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
          window.location.replace('/login');
          return;
        }
        $message.text('Không thể tải danh sách người dùng. Vui lòng thử lại.').addClass('error');
      }).always(() => setBusy($button, false));
    });

    $('#logout').on('click', function () {
      localStorage.removeItem(tokenKey);
      localStorage.removeItem(expiryKey);
      window.location.assign('/login');
    });
  }
});
