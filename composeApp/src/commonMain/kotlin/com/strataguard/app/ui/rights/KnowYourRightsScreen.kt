package com.strataguard.app.ui.rights

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strataguard.app.ui.theme.Amber500
import com.strataguard.app.ui.theme.Grey200
import com.strataguard.app.ui.theme.Grey700
import com.strataguard.app.ui.theme.Navy50
import com.strataguard.app.ui.theme.Navy800
import com.strataguard.app.ui.theme.Navy900

// ── Data model ──────────────────────────────────────────────────────────────

private enum class SectionStyle { BULLETS, NUMBERED, DEADLINE_PAIRS, LINKS }

private data class RightsSection(
    val emoji: String,
    val title: String,
    val style: SectionStyle,
    val items: List<String>,
)

private data class StateContent(
    val state: String,
    val tribunal: String,
    val tribunalFull: String,
    val legislation: String,
    val sections: List<RightsSection>,
)

private val nswContent = StateContent(
    state = "NSW",
    tribunal = "NCAT",
    tribunalFull = "NSW Civil and Administrative Tribunal",
    legislation = "Strata Schemes Management Act 2015",
    sections = listOf(
        RightsSection(
            emoji = "⚖️",
            title = "Your Rights as a Resident",
            style = SectionStyle.BULLETS,
            items = listOf(
                "Right to a safe, habitable building maintained in good repair at the owners corporation's expense",
                "Right to inspect strata records: strata roll, financial statements, meeting minutes, by-laws",
                "Tenants can attend AGMs as observers and raise concerns in writing via their landlord",
                "24 hours' written notice required before entry to your lot (48 hours for routine inspections)",
                "Right to apply to NCAT if the owners corporation fails to maintain common property",
                "Protection against unreasonable by-law enforcement — by-laws cannot be harsh, unconscionable or oppressive",
            ),
        ),
        RightsSection(
            emoji = "🏛️",
            title = "What NCAT Can Resolve",
            style = SectionStyle.BULLETS,
            items = listOf(
                "Failure to repair or maintain common property (e.g. leaking roof, broken lifts, mould from building defects)",
                "Breach of by-laws — noise, pets, parking, renovations without consent",
                "Disputes about levy amounts or special levy calculations",
                "Unreasonable refusal to approve minor renovations",
                "Noise nuisance or harassment from another lot owner",
                "Disputes over the strata committee's decisions or conduct",
                "Bond claims involving damage attributable to building defects",
            ),
        ),
        RightsSection(
            emoji = "📋",
            title = "How to File at NCAT",
            style = SectionStyle.NUMBERED,
            items = listOf(
                "Document everything — photos, dates, written complaints, responses received",
                "Write formally to the owners corporation / strata manager requesting rectification",
                "Apply for free mediation through NSW Fair Trading (mandatory before most NCAT applications)",
                "If mediation fails, apply online at ncat.nsw.gov.au under 'Strata and community schemes'",
                "Pay the application fee — from \$57 for standard matters",
                "Serve the application on the other party and attend the hearing (in person or video)",
            ),
        ),
        RightsSection(
            emoji = "⏰",
            title = "Key Deadlines",
            style = SectionStyle.DEADLINE_PAIRS,
            items = listOf(
                "By-law breach|Apply within 12 months of becoming aware of the breach",
                "Bond dispute|Within 3 months of the tenancy ending (Tribunal fast-tracks bond claims)",
                "Building defect|No hard deadline — but delays weaken your evidence; act within 2 years",
                "Levy dispute|Apply promptly after receiving the levy notice; delays may limit relief",
                "Appeal an NCAT order|Within 28 days of the decision",
            ),
        ),
        RightsSection(
            emoji = "🔗",
            title = "Resources & Contacts",
            style = SectionStyle.LINKS,
            items = listOf(
                "NCAT — Apply online|ncat.nsw.gov.au",
                "NSW Fair Trading — Mediation|fairtrading.nsw.gov.au",
                "Tenants NSW — Free advice|tenants.org.au",
                "NSW Strata Hub — Building records|strata.nsw.gov.au",
                "Community Justice Centres — Free mediation|cjc.nsw.gov.au",
            ),
        ),
    ),
)

private val vicContent = StateContent(
    state = "VIC",
    tribunal = "VCAT",
    tribunalFull = "Victorian Civil and Administrative Tribunal",
    legislation = "Owners Corporations Act 2006",
    sections = listOf(
        RightsSection(
            emoji = "⚖️",
            title = "Your Rights as a Resident",
            style = SectionStyle.BULLETS,
            items = listOf(
                "Right to a well-maintained building — owners corporation must repair and maintain common property",
                "Right to inspect owners corporation records: register, rules, financial statements, minutes",
                "Owner-occupiers and landlords can attend general meetings and vote on decisions",
                "At least 14 days' notice required before entry to your lot for non-emergency work",
                "Right to apply to VCAT if the owners corporation breaches its maintenance obligations",
                "Owners corporation rules must not be unreasonable — VCAT can invalidate oppressive rules",
            ),
        ),
        RightsSection(
            emoji = "🏛️",
            title = "What VCAT Can Resolve",
            style = SectionStyle.BULLETS,
            items = listOf(
                "Failure to maintain or repair common property (structural issues, waterproofing, shared services)",
                "Breach of owners corporation rules — noise, pets, short-stay rentals, parking",
                "Unreasonable levy amounts or disputes about special levies",
                "Disputes about lot owner renovations affecting common property or other lots",
                "Disputes between lot owners about shared spaces or conduct",
                "Disputes about the owners corporation manager's conduct or fees",
                "Appeals against owners corporation committee decisions",
            ),
        ),
        RightsSection(
            emoji = "📋",
            title = "How to File at VCAT",
            style = SectionStyle.NUMBERED,
            items = listOf(
                "Document the issue thoroughly — photos, written timeline, any correspondence",
                "Write formally to the owners corporation manager requesting resolution",
                "Request internal dispute resolution (required before VCAT for most matters)",
                "Apply online at vcat.vic.gov.au under 'Owners Corporations'",
                "Pay the application fee — from \$74.50 for standard applications",
                "Serve the application on the other party and attend the hearing (in person, phone, or video)",
            ),
        ),
        RightsSection(
            emoji = "⏰",
            title = "Key Deadlines",
            style = SectionStyle.DEADLINE_PAIRS,
            items = listOf(
                "Owners corporation rule breach|Apply within 2 years of becoming aware of the breach",
                "Levy dispute|Apply within 12 months of receiving the levy notice",
                "Bond / tenancy dispute|Within 3 months of the tenancy ending (via VCAT Residential Tenancies list)",
                "Building defect|No hard deadline — act within 2 years; longer delays weaken your case",
                "Appeal a VCAT order|Within 28 days of the final order",
            ),
        ),
        RightsSection(
            emoji = "🔗",
            title = "Resources & Contacts",
            style = SectionStyle.LINKS,
            items = listOf(
                "VCAT — Apply online|vcat.vic.gov.au",
                "Consumer Affairs Victoria — Owners Corporations|consumer.vic.gov.au",
                "Tenants Victoria — Free advice|tenantsvic.org.au",
                "Owners Corporations Register|consumer.vic.gov.au/housing/owners-corporations",
                "Dispute Settlement Centre Victoria — Free mediation|disputes.vic.gov.au",
            ),
        ),
    ),
)

// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowYourRightsScreen(onNavigateBack: () -> Unit) {
    var selectedState by remember { mutableStateOf("NSW") }
    val content = if (selectedState == "NSW") nswContent else vicContent

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Know Your Rights", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy900),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            item {
                StateHeader(content = content, selectedState = selectedState, onStateSelected = { selectedState = it })
            }
            itemsIndexed(content.sections) { _, section ->
                RightsSectionCard(section = section)
            }
        }
    }
}

@Composable
private fun StateHeader(
    content: StateContent,
    selectedState: String,
    onStateSelected: (String) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Navy900)
            .padding(horizontal = 20.dp)
            .padding(top = 4.dp, bottom = 20.dp),
    ) {
        // State toggle
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("NSW", "VIC").forEach { s ->
                val selected = selectedState == s
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (selected) Amber500 else Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.clickable { onStateSelected(s) },
                ) {
                    Text(
                        text = s,
                        color = Color.White,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(content.tribunalFull, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(2.dp))
        Text(content.legislation, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
    }
}

@Composable
private fun RightsSectionCard(section: RightsSection) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp),
    ) {
        Column {
            // Header row — always visible, tap to expand/collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(section.emoji, fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    section.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp),
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    HorizontalDivider(color = Grey200)
                    Spacer(Modifier.height(12.dp))

                    when (section.style) {
                        SectionStyle.BULLETS   -> section.items.forEach { BulletItem(it) }
                        SectionStyle.NUMBERED  -> section.items.forEachIndexed { i, text -> NumberedItem(i + 1, text) }
                        SectionStyle.DEADLINE_PAIRS -> section.items.forEach { DeadlineItem(it) }
                        SectionStyle.LINKS     -> section.items.forEach { LinkItem(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun BulletItem(text: String) {
    Row(
        modifier = Modifier.padding(bottom = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            Modifier
                .padding(top = 7.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(Amber500),
        )
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 13.sp, color = Grey700, lineHeight = 19.sp)
    }
}

@Composable
private fun NumberedItem(number: Int, text: String) {
    Row(
        modifier = Modifier.padding(bottom = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Navy800),
            contentAlignment = Alignment.Center,
        ) {
            Text("$number", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 13.sp, color = Grey700, lineHeight = 19.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun DeadlineItem(raw: String) {
    val parts = raw.split("|")
    if (parts.size < 2) return
    Column(Modifier.padding(bottom = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Navy50, shape = RoundedCornerShape(4.dp)) {
                Text(
                    parts[0],
                    color = Navy800,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(parts[1], fontSize = 13.sp, color = Grey700, lineHeight = 18.sp)
    }
}

@Composable
private fun LinkItem(raw: String) {
    val parts = raw.split("|")
    if (parts.size < 2) return
    Row(
        modifier = Modifier.padding(bottom = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(color = Amber500.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
            Text(
                "↗",
                color = Amber500,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(parts[0], fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Navy800)
            Text(parts[1], fontSize = 11.sp, color = Color.Gray)
        }
    }
}
