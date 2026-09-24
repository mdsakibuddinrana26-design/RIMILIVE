package com.example.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RimiInviteScreenTest {
    @Test fun inviteCodeIsStableUidAndBlankWhenSignedOut() {
        assertEquals("firebase-uid", rimiInviteCode("firebase-uid"))
        assertEquals("", rimiInviteCode(null))
    }

    @Test fun selfReferralAndMissingCodesAreRejected() {
        assertFalse(canRegisterRimiReferral("same", "same"))
        assertFalse(canRegisterRimiReferral(null, "inviter"))
        assertFalse(canRegisterRimiReferral("invitee", ""))
        assertTrue(canRegisterRimiReferral("invitee", "inviter"))
    }
}