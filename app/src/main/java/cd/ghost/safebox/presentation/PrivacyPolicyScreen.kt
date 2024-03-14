package cd.ghost.safebox.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cd.ghost.safebox.R
import cd.ghost.safebox.presentation.components.CorneredButton

val privacyPolicy = """
    Privacy Policy for SafeBox Application

    Introduction

    Thank you for choosing SafeBox, a privacy-focused application designed to help you secure and manage your sensitive data on your Android device. This Privacy Policy outlines how SafeBox collects, uses, and safeguards your personal information.

    Information We Collect

    1. User-Provided Information:
       - SafeBox requires users to set up a password to access the application. This password is securely stored on the device and is not accessible by SafeBox or any third party.
       - Users can input and store sensitive data within the application, such as text, documents, or other files.

    2. Automatically Collected Information:
       - SafeBox may collect anonymous usage data to improve the application's performance and user experience. This data is solely used for analytical purposes and does not include personal information.

    How We Use Your Information

    1. Password Protection:
       - Your password is used to secure access to the SafeBox application. It is not stored or transmitted to any external servers.

    2. Data Encryption:
       - SafeBox utilizes Android's Cipher functionality to encrypt and decrypt your sensitive data stored on the device. Encryption is applied to enhance the security of your information.

    3. Local Storage:
       - All user data, including encrypted files, is stored within the internal file directory of the SafeBox application on the user's device. SafeBox does not access, share, or transmit this data to external servers.

    4. Improving Application Performance:
       - Anonymous usage data may be collected to analyze application performance and identify areas for improvement. This data is used solely for enhancing the user experience.

    Security Measures

    1. Device-Level Security:
       - SafeBox relies on the security features provided by the Android operating system, including device-level encryption and secure key storage.

    2. Secure Data Transmission:
       - SafeBox does not transmit your data over the internet. All encryption and decryption processes occur locally on your device.

    Third-Party Services

    SafeBox does not integrate with any third-party services or applications. Your data remains within the confines of the SafeBox application on your device.

    Children's Privacy

    SafeBox is not intended for use by children under the age of 13. We do not knowingly collect personal information from children. If you believe that a child has provided us with personal information, please contact us, and we will take steps to delete such information.

    Changes to This Privacy Policy

    We reserve the right to update our Privacy Policy to reflect changes to our information practices. Users will be notified of any material changes to this Privacy Policy via a notice within the SafeBox application.

    Contact Us

    If you have any questions or concerns regarding this Privacy Policy, please contact us at [insert contact information].

    Thank you for choosing SafeBox for your privacy needs.
""".trimIndent()

val privacyPolicyMod = """
    <h1 style="color: #1976D2;">Privacy Policy for SafeBox Application</h1>

    <p><b>Effective Date:</b> [Insert Date]</p>

    <h2 style="color: #1976D2;">Introduction</h2>

    <p>Thank you for choosing SafeBox, a privacy-focused application designed to help you secure and manage your sensitive data on your Android device. This Privacy Policy outlines how SafeBox collects, uses, and safeguards your personal information.</p>

    <!-- Other sections with styling -->

    <h2 style="color: #1976D2;">Security Measures</h2>

    <ol>
        <li><b>Device-Level Security:</b> SafeBox relies on the security features provided by the Android operating system, including device-level encryption and secure key storage.</li>
        <li><b>Secure Data Transmission:</b> SafeBox does not transmit your data over the internet. All encryption and decryption processes occur locally on your device.</li>
    </ol>

    <h2 style="color: #1976D2;">Third-Party Services</h2>

    <p>SafeBox does not integrate with any third-party services or applications. Your data remains within the confines of the SafeBox application on your device.</p>

    <h2 style="color: #1976D2;">Children's Privacy</h2>

    <p>SafeBox is not intended for use by children under the age of 13. We do not knowingly collect personal information from children. If you believe that a child has provided us with personal information, please contact us, and we will take steps to delete such information.</p>

    <h2 style="color: #1976D2;">Changes to This Privacy Policy</h2>

    <p>We reserve the right to update our Privacy Policy to reflect changes to our information practices. Users will be notified of any material changes to this Privacy Policy via a notice within the SafeBox application.</p>

    <h2 style="color: #1976D2;">Contact Us</h2>

    <p>If you have any questions or concerns regarding this Privacy Policy, please contact us at [insert contact information].</p>

    <p>Thank you for choosing SafeBox for your privacy needs.</p>
""".trimIndent()

@Composable
fun PrivacyPolicyScreen(
    onNavigate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = painterResource(R.drawable.img_privacy_policy),
            contentDescription = null
        )
        Text(
            modifier = Modifier.padding(8.dp),
            text = privacyPolicy,
        )
        CorneredButton(
            onClick = onNavigate
        ) {
            Text(
                text = "Confirm",
                color = Color.White
            )
        }
    }
}