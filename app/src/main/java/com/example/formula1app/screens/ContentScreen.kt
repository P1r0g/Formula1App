package com.example.formula1app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.formula1app.R
import com.example.formula1app.data.DriverStanding
import com.example.formula1app.ui.teamColor
import java.text.NumberFormat

@Composable
fun ContentScreen(
    standings: List<DriverStanding>,
    onOpenSettings: () -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]

    val pointsFormat = remember(locale) {
        NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 1
            isGroupingUsed = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.standings_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall
                )

                TextButton(onClick = onOpenSettings) {
                    Text(
                        text = stringResource(R.string.settings_action)
                    )
                }
            }

            Text(
                text = stringResource(R.string.standings_demo_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (standings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.standings_empty),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                StandingColumns(
                    position = stringResource(R.string.column_position),
                    driver = stringResource(R.string.column_driver),
                    team = stringResource(R.string.column_team),
                    points = stringResource(R.string.column_points),
                    isHeader = true
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(
                        items = standings,
                        key = { standing -> standing.driverId }
                    ) { standing ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = 2.dp,
                                color = teamColor(standing.teamId)
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            StandingColumns(
                                position = standing.position?.toString() ?: "—",
                                driver = standing.driverName,
                                team = standing.teamName,
                                points = pointsFormat.format(standing.points)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StandingColumns(
    position: String,
    driver: String,
    team: String,
    points: String,
    isHeader: Boolean = false
) {
    val textStyle = if (isHeader) {
        MaterialTheme.typography.labelMedium
    } else {
        MaterialTheme.typography.bodyMedium
    }

    val textColor = if (isHeader) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = position,
            modifier = Modifier.width(40.dp),
            style = textStyle,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Text(
            text = driver,
            modifier = Modifier.weight(1.4f),
            style = textStyle,
            color = textColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = team,
            modifier = Modifier.weight(1f),
            style = if (isHeader) {
                textStyle
            } else {
                MaterialTheme.typography.bodySmall
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = points,
            modifier = Modifier.width(48.dp),
            style = textStyle,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}