package com.rangerscards.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rangerscards.R
import com.rangerscards.domain.model.User
import com.rangerscards.ui.components.RangersDialogWithContent
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.settings.SettingsUiState
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost

@Composable
fun AccountCard(
    isDarkTheme: Boolean,
    user: User,
    settingsUiState: SettingsUiState,
    signIn: (String, String) -> Unit,
    signOut: () -> Unit,
    sendPasswordResetEmail: (String) -> Unit,
    createAccount: (String, String) -> Unit,
    deleteAccount: (String, String) -> Unit,
    updateHandle: (String, String) -> Unit,
    navigateToFriends: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var openAuthDialog by rememberSaveable { mutableStateOf(false) }
    var openHandleDialog by rememberSaveable { mutableStateOf(false) }
    var userHandle by remember(user.userInfo) { mutableStateOf(user.userInfo?.handle ?: "") }
    var isDeleting by rememberSaveable { mutableStateOf(false) }
    var forgotPassword by rememberSaveable { mutableStateOf(false) }

    if (forgotPassword) RangersDialogWithContent(
        headerId = R.string.forgot_password,
        isDarkTheme = isDarkTheme,
        onBack = { forgotPassword = false }
    ) {
        SettingsInputField(
            leadingIcon = Icons.Filled.Email,
            placeholder = R.string.email_placeholder,
            textValue = email,
            onValueChange = { email = it },
            KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            )
        )
        SquareButton(
            R.string.send_reset_link,
            R.drawable.done_32dp,
            onClick = {
                sendPasswordResetEmail(email)
                forgotPassword = false
                email = ""
            }
        )
    }

    if (openAuthDialog && user.userInfo == null) RangersDialogWithContent(
        headerId = R.string.sign_in_up_to_app_account_title,
        isDarkTheme = isDarkTheme,
        onBack = { openAuthDialog = false }
    ) {
        EmailAndPasswordInputFields(
            email = email,
            onEmailChange = { email = it },
            password = password,
            onPasswordChange = { password = it }
        )
        SquareButton(
            R.string.sign_in_to_account_button,
            R.drawable.login_32dp,
            onClick = {
                signIn(email, password)
                openAuthDialog = false
                email = ""; password = ""
            }
        )
        SquareButton(
            R.string.sign_up_to_app_account_button,
            R.drawable.add_32dp,
            onClick = {
                createAccount(email, password)
                openAuthDialog = false
                email = ""; password = ""
            }
        )
        SquareButton(
            R.string.forgot_password,
            R.drawable.info_32dp,
            onClick = {
                openAuthDialog = false
                forgotPassword = true
                password = ""
            }
        )
    } else if (openAuthDialog) RangersDialogWithContent(
        headerId = if (isDeleting) R.string.delete_account_title else R.string.sign_out_account_title,
        isDarkTheme = isDarkTheme,
        onBack = { openAuthDialog = false; isDeleting = false }
    ) {
        if (isDeleting) {
            EmailAndPasswordInputFields(
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it }
            )
            SquareButton(
                stringId = R.string.cancel_button,
                leadingIcon = R.drawable.close_32dp,
                onClick = { openAuthDialog = false; isDeleting = false },
                buttonColor = ButtonDefaults.buttonColors()
                    .copy(CustomTheme.colors.d30),
                iconColor = CustomTheme.colors.warn,
                textColor = CustomTheme.colors.l30
            )
            SquareButton(
                R.string.delete_account_button,
                R.drawable.delete_32dp,
                onClick = {
                    deleteAccount(email, password)
                    openAuthDialog = false; isDeleting = false
                    email = ""; password = ""
                },
                buttonColor = ButtonDefaults.buttonColors()
                    .copy(CustomTheme.colors.warn),
                iconColor = if (isDarkTheme)
                    CustomTheme.colors.d30 else CustomTheme.colors.l30,
                textColor = if (isDarkTheme)
                    CustomTheme.colors.d30 else CustomTheme.colors.l30
            )
        } else {
            Text(
                text = stringResource(id = R.string.sign_out_account_text),
                color = CustomTheme.colors.d30,
                fontFamily = Jost,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                modifier = modifier.padding(horizontal = 4.dp)
            )
            SquareButton(
                stringId = R.string.cancel_button,
                leadingIcon = R.drawable.close_32dp,
                onClick = { openAuthDialog = false },
                buttonColor = ButtonDefaults.buttonColors()
                    .copy(CustomTheme.colors.d30),
                iconColor = CustomTheme.colors.warn,
                textColor = CustomTheme.colors.l30
            )
            SquareButton(
                R.string.delete_account_button,
                R.drawable.delete_32dp,
                onClick = {
                    isDeleting = true
                },
                buttonColor = ButtonDefaults.buttonColors()
                    .copy(CustomTheme.colors.warn),
                iconColor = if (isDarkTheme)
                    CustomTheme.colors.d30 else CustomTheme.colors.l30,
                textColor = if (isDarkTheme)
                    CustomTheme.colors.d30 else CustomTheme.colors.l30
            )
            SquareButton(
                stringId = R.string.sign_out_account_button,
                leadingIcon = R.drawable.logout_32dp,
                onClick = {
                    signOut()
                    openAuthDialog = false
                    userHandle = ""
                }
            )
        }
    }

    if (openHandleDialog) RangersDialogWithContent(
        headerId = R.string.account_name_header,
        isDarkTheme = isDarkTheme,
        onBack = { openHandleDialog = false }
    ) {
        SettingsInputField(
            leadingIcon = R.drawable.badge_32dp,
            placeholder = null,
            textValue = userHandle,
            onValueChange = { userHandle = it },
            KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SquareButton(
                stringId = R.string.cancel_button,
                leadingIcon = R.drawable.close_32dp,
                onClick = { openHandleDialog = false },
                buttonColor = ButtonDefaults.buttonColors().copy(
                    CustomTheme.colors.d30,
                    disabledContainerColor = CustomTheme.colors.m
                ),
                iconColor = CustomTheme.colors.warn,
                textColor = CustomTheme.colors.l30,
                modifier = Modifier.weight(0.5f),
            )
            SquareButton(
                stringId = R.string.done_button,
                leadingIcon = R.drawable.done_32dp,
                onClick = {
                    if (user.userInfo?.handle != userHandle) {
                        updateHandle(user.userInfo?.id!!, userHandle)
                    }
                    openHandleDialog = false },
                buttonColor = ButtonDefaults.buttonColors().copy(
                    CustomTheme.colors.d10,
                    disabledContainerColor = CustomTheme.colors.m
                ),
                iconColor = CustomTheme.colors.l15,
                textColor = CustomTheme.colors.l30,
                modifier = Modifier.weight(0.5f),
            )
        }
    }

    RangersBaseCard(
        isDarkTheme = isDarkTheme,
        labelIdRes = R.string.account_title
    ) {
        if (user.userInfo == null) {
            TextWhenNotLoggedIn()
            SquareButton(
                stringId = R.string.sign_in_to_app_account_button,
                leadingIcon = R.drawable.login_32dp,
                onClick = { openAuthDialog = true }
            )
        } else {
            Column(
                modifier = Modifier.background(
                    if (isDarkTheme) CustomTheme.colors.l15 else CustomTheme.colors.l20,
                    CustomTheme.shapes.large
                ),
            ) {
                if (settingsUiState is SettingsUiState.Loading) Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        Modifier.size(32.dp),
                        color = CustomTheme.colors.m
                    )
                }
                else SettingsClickableSurface(
                    leadingIcon = R.drawable.badge_32dp,
                    trailingIcon = R.drawable.edit_32dp,
                    headerId = R.string.account_name_header,
                    text = user.userInfo?.handle ?: "",
                ) {
                    userHandle = user.userInfo?.handle ?: ""
                    openHandleDialog = true
                }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = CustomTheme.colors.l10
                )
                if (settingsUiState is SettingsUiState.Loading) Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        Modifier.size(32.dp),
                        color = CustomTheme.colors.m
                    )
                }
                else {
                    val friendsCount = user.friends.size
                    val friendRequestCount = user.receivedRequests.size
                    SettingsClickableSurface(
                        leadingIcon = R.drawable.group_32dp,
                        trailingIcon = R.drawable.add_32dp,
                        headerId = R.string.friends_amount_header,
                        text = pluralStringResource(
                            id = R.plurals.friends_amount,
                            count = friendsCount,
                            friendsCount
                        ) + if (friendRequestCount > 0) " + ${pluralStringResource(
                            id = R.plurals.friends_request_amount,
                            count = friendRequestCount,
                            friendRequestCount
                        )}" else "",
                        onClick = navigateToFriends
                    )
                }
            }
            SquareButton(
                stringId = R.string.sign_out_account_button,
                leadingIcon = R.drawable.logout_32dp,
                onClick = { openAuthDialog = true },
            )
        }
    }
}

@Composable
fun EmailAndPasswordInputFields(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit
) {
    SettingsInputField(
        leadingIcon = Icons.Filled.Email,
        placeholder = R.string.email_placeholder,
        textValue = email,
        onValueChange = onEmailChange,
        KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
        )
    )
    SettingsInputField(
        leadingIcon = Icons.Filled.Lock,
        placeholder = R.string.password_placeholder,
        textValue = password,
        onValueChange = onPasswordChange,
        KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
        visualTransformation = PasswordVisualTransformation()
    )
}